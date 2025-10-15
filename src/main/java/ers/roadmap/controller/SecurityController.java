package ers.roadmap.controller;

import ers.roadmap.DTO.*;
import ers.roadmap.DTO.mappers.AppUserMapper;
import ers.roadmap.exceptions.LoginFormException;
import ers.roadmap.exceptions.VerifyEmailException;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.service.AppUserService;
import ers.roadmap.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/security")
public class SecurityController {

    private final AppUserService userService;
    private final AppUserMapper userMapper;
    private final EmailService emailService;

    public SecurityController(AppUserService userService, AppUserMapper userMapper, EmailService emailService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.emailService = emailService;
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationForm registrationForm, BindingResult br) {
        long t0 = System.currentTimeMillis();

        if (br.hasErrors()) {
            return new ResponseEntity<>(new CustomMessage("Incorrect registration form"), HttpStatus.BAD_REQUEST);
        }

        try {
            long t1 = System.currentTimeMillis();
            AppUser notVerifiedUser = userService.createUser(registrationForm);
            long t2 = System.currentTimeMillis();
            System.out.println("createUser time: " + (t2 - t1) + " ms");

            // Измерим отправку письма отдельно
            long t3 = System.currentTimeMillis();
            emailService.sendMessageAsync(notVerifiedUser);
            long t4 = System.currentTimeMillis();
            System.out.println("sendMessage time: " + (t4 - t3) + " ms");

            System.out.println("total: " + (t4 - t0) + " ms");
            return new ResponseEntity<>(new CustomMessage("Verification code is sent, if email exists"), HttpStatus.CREATED);

        } catch (LoginFormException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/public/logout")
    public ResponseEntity<?> logout() {
        System.out.println("Logout");
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .path("/")       // должен совпадать с path при создании
                .maxAge(0)       // удаляет куку
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new CustomMessage("Logged out successfully"));
    }

    @PostMapping("/public/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO input) {

        System.out.println("Verifying user with email : " + input.getEmail());

        try {
            String jwt = userService.verifyUserAndGetJwt(input);

            System.out.println("User with email " + input.getEmail() + " got verified" );

            return userService.setJwtToCookie(jwt);

        }catch (VerifyEmailException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/public/verification-code")
    public ResponseEntity<?> resendCode(@RequestBody ResendEmailDTO input) {

        System.out.println("User with email : " + input.getEmail() + " is trying to resend the verification code");

        try {
            AppUser notVerifiedUser = userService.regenetrateVerificationCode(input);
            emailService.sendMessageAsync(notVerifiedUser);
            return new ResponseEntity<>(new CustomMessage("Verification code sent!"), HttpStatus.OK);
        }catch (VerifyEmailException e) {
            System.out.println("Something went wrong while trying to resend verification code to " + input.getEmail());
            System.out.println("Cause : " + e.getMessage());
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/public/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginForm loginForm, BindingResult br) {

        System.out.println("User " + loginForm.getUsername() + " try to log in");

        if(br.hasErrors()) {
            System.out.println("User " + loginForm.getUsername() + " had bad login form");

            return new ResponseEntity<>(new CustomMessage("Incorrect login form"), HttpStatus.BAD_REQUEST);
        }

        try{
            String jwt = userService.loginJwt(loginForm);

            System.out.println("User " + loginForm.getUsername() + " logged in");
            return userService.setJwtToCookie(jwt);

        }catch (LoginFormException e) {
            System.out.println("User " + loginForm.getUsername() + " had bad credentials");
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/data")
    public ResponseEntity<?> getInfo(@AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("Called security data to : " + userDetails.getUsername());
        AppUser owner;

        try {
            owner = userService.findByUsername(userDetails.getUsername());
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new CustomMessage("User not found"), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(userMapper.toDto(owner), HttpStatus.OK);

    }

    @GetMapping("/users")
    public List<AppUserDTO> getAll() {
        return userService.getAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

}
