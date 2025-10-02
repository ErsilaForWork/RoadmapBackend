package ers.roadmap.security.service;

import ers.roadmap.DTO.LoginForm;
import ers.roadmap.DTO.RegistrationForm;
import ers.roadmap.exceptions.LoginFormException;
import ers.roadmap.jwt.JwtUtils;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.security.model.AppRole;
import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.model.EnumAppRole;
import ers.roadmap.security.repo.AppRoleRepo;
import ers.roadmap.security.repo.AppUserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepo userRepo;
    private final AppRoleRepo roleRepo;
    private final PasswordEncoder encoder;
    private final AppRole ROLE_USER;
    private final AppRole ROLE_ADMIN;
    private final JwtUtils jwtUtils;
    private final int jwtExpiryMillis;

    public AppUserService(AppUserRepo userRepo, AppRoleRepo roleRepo, PasswordEncoder encoder, JwtUtils jwtUtils, @Value("${jwt.expire}") int jwtExpriyMillis) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        ROLE_USER = roleRepo.getAppRoleByRole(EnumAppRole.ROLE_USER);
        ROLE_ADMIN = roleRepo.getAppRoleByRole(EnumAppRole.ROLE_ADMIN);
        this.jwtUtils = jwtUtils;
        this.jwtExpiryMillis = jwtExpriyMillis;
    }


    //Searches by username, if not found throws UsernameNotFoundException
    public AppUser findByUsername(String username) throws UsernameNotFoundException{

        return userRepo.findAppUserByUsernameWithRoadmaps(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found!")
        );

    }

    //Creates and saves user in the DB, and returns generated JWT token;
    @Transactional
    public String createAndSaveUserJWT(RegistrationForm registrationForm) throws LoginFormException {

        //Throws LoginFormException if unique constraints are not respected
        checkForUniqueConstraintsRegistrationForm(registrationForm);

        //Race condition safety
        try{
            AppUser user = new AppUser(registrationForm, ROLE_USER);
            user.setPassword(encoder.encode(user.getPassword()));
            userRepo.save(user);
            return jwtUtils.generateTokenFromUsername(user.getUsername());
        }catch (DataIntegrityViolationException e) {
            throw new LoginFormException("User already exists");
        }
    }

    // Logs user in by returning JWT token if credentials are valid
    public String loginJwt(LoginForm loginForm) throws LoginFormException{

        //Throws UsernameNotFoundException, IncorrectPasswordException
        checkForCorrectLoginForm(loginForm);

        return jwtUtils.generateTokenFromUsername(loginForm.getUsername());

    }

    // Checks if username exists and if password is correct
    private void checkForCorrectLoginForm(LoginForm loginForm) throws LoginFormException {

        AppUser user = userRepo.findAppUserByUsername(loginForm.getUsername()).orElseThrow(
                () -> new LoginFormException("Invalid username or password")
        );

        // If passwords dont match
        if(!encoder.matches(loginForm.getPassword(), user.getPassword())) {
            throw new LoginFormException("Invalid username or password");
        }
    }

    //Checks for unique constraints of Login Form
    private void checkForUniqueConstraintsRegistrationForm(RegistrationForm registrationForm) throws LoginFormException {

        if(userRepo.existsByUsername(registrationForm.getUsername())){
            throw new LoginFormException("Username is already exists!");
        }

        if(userRepo.existsByEmail(registrationForm.getEmail())) {
            throw new LoginFormException("Email is already used!");
        }

    }

    // To return Response Entity with correct cookies
    public ResponseEntity<?> setJwtToCookie(String jwt) {
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .path("/")
                .maxAge(jwtExpiryMillis / 1000)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new CustomMessage("Successful"));
    }

    public List<AppUser> getAll() {
        return userRepo.getAllWithRoadmap();
    }

    public List<AppUser> getAllSimpleRepo() {
        return userRepo.findAll();
    }
}
