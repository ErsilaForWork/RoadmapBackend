package ers.roadmap.controller;

import ers.roadmap.DTO.AppUserDTO;
import ers.roadmap.DTO.LoginForm;
import ers.roadmap.DTO.RegistrationForm;
import ers.roadmap.DTO.VerifyUserDTO;
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

        System.out.println("User " + registrationForm.getUsername() + " try to register");

        if(br.hasErrors()) {
            System.out.println("User " + registrationForm.getUsername() + " had bad registration form");
            return new ResponseEntity<>(new CustomMessage("Incorrect registration form"), HttpStatus.BAD_REQUEST);
        }

        try{
            AppUser notVerifiedUser = userService.createUser(registrationForm);
            System.out.println("User " + notVerifiedUser.getUsername() + " successfully created");
            emailService.sendMessage(notVerifiedUser);
            System.out.println("Message to verify user " + notVerifiedUser.getUsername() + " is sent!");
            userService.save(notVerifiedUser);
            System.out.println("User " + notVerifiedUser.getUsername() + " is saved to DB");
            return new ResponseEntity<>(new CustomMessage("Now verify your email;"), HttpStatus.CREATED);

        }catch (LoginFormException e) {
            System.out.println("User " + registrationForm.getUsername() + " already exists");
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
    public ResponseEntity<?> resendCode(@RequestBody VerifyUserDTO input) {

        System.out.println("User with email : " + input.getEmail() + " is trying to resend the verification code");

        try {
            AppUser notVerifiedUser = userService.regenetrateVerificationCode(input);
            emailService.sendMessage(notVerifiedUser);
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
