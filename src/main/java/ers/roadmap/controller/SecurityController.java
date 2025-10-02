package ers.roadmap.controller;

import ers.roadmap.DTO.AppUserDTO;
import ers.roadmap.DTO.LoginForm;
import ers.roadmap.DTO.RegistrationForm;
import ers.roadmap.DTO.mappers.AppUserMapper;
import ers.roadmap.exceptions.LoginFormException;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    public SecurityController(AppUserService userService, AppUserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationForm registrationForm, BindingResult br) {

        if(br.hasErrors()) {
            return new ResponseEntity<>(new CustomMessage("Incorrect registration form"), HttpStatus.BAD_REQUEST);
        }

        try{
            String jwt = userService.createAndSaveUserJWT(registrationForm);

            return userService.setJwtToCookie(jwt);

        }catch (LoginFormException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.CONFLICT);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginForm loginForm, BindingResult br) {

        if(br.hasErrors()) {
            return new ResponseEntity<>(new CustomMessage("Incorrect login form"), HttpStatus.BAD_REQUEST);
        }

        try{
            String jwt = userService.loginJwt(loginForm);

            return userService.setJwtToCookie(jwt);

        }catch (LoginFormException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/data")
    public ResponseEntity<?> getInfo(@AuthenticationPrincipal UserDetails userDetails) {

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
