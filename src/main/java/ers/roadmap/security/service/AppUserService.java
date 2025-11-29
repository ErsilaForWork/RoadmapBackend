package ers.roadmap.security.service;

import ers.roadmap.DTO.LoginForm;
import ers.roadmap.DTO.RegistrationForm;
import ers.roadmap.DTO.ResendEmailDTO;
import ers.roadmap.DTO.VerifyUserDTO;
import ers.roadmap.exceptions.LoginFormException;
import ers.roadmap.exceptions.VerifyEmailException;
import ers.roadmap.jwt.JwtUtils;
import ers.roadmap.model.CustomMessage;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AppUserService {

    private final AppUserRepo userRepo;
    private final AppRoleRepo roleRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final int jwtExpiryMillis;

    public AppUserService(AppUserRepo userRepo, AppRoleRepo roleRepo, PasswordEncoder encoder, JwtUtils jwtUtils, @Value("${jwt.expire}") int jwtExpriyMillis) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.jwtExpiryMillis = jwtExpriyMillis;
    }


    //Searches by username, if not found throws UsernameNotFoundException
    public AppUser findByUsername(String username) throws UsernameNotFoundException{

        return userRepo.findAppUserByUsernameWithRoadmaps(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found!")
        );

    }

    public AppUser createUser(RegistrationForm registrationForm) {

        AppUser user = new AppUser(registrationForm, roleRepo.getAppRoleByRole(EnumAppRole.ROLE_USER));
        user.setPassword(encoder.encode(user.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpires(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        return saveWithCheck(user);

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

        if(!user.isEnabled()) {
            throw new LoginFormException("Email is not verificated!");
        }

        // If passwords dont match
        if(!encoder.matches(loginForm.getPassword(), user.getPassword())) {
            throw new LoginFormException("Invalid username or password");
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

    public void setStreakBroken(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such username!"));


        if(user.getLastActivityDate().isBefore(LocalDate.now().minusDays(1)) && user.getStreak() > 1) {
            user.setStreakBroken(true);
        }

        user.setLastLoginDate(LocalDate.now());
        userRepo.save(user);
    }

    public void incrementStreak(AppUser user) {

        LocalDate lastActivity = user.getLastActivityDate();
        LocalDate now = LocalDate.now();

        //If last activity date is today
        if(lastActivity.equals(now)) return;

        //If last activity date is yesterday
        if(lastActivity.equals(now.minusDays(1)) && !user.isStreakBroken()) {
            user.setStreak(user.getStreak() + 1);
        }else {
            user.setStreakBroken(false);
            user.setStreak(1);
        }

        user.setLastActivityDate(now);
        userRepo.save(user);
    }

    @Transactional
    public String verifyUserAndGetJwt(VerifyUserDTO input) throws VerifyEmailException {

        Optional<AppUser> userOptional = userRepo.findByEmail(input.getEmail());

        if(userOptional.isEmpty()) {
            throw new VerifyEmailException("No user found with email:"+input.getEmail());
        }

        AppUser user = userOptional.get();

        if(user.isEnabled()) {
            throw new VerifyEmailException("User already verified his email!");
        }

        if(LocalDateTime.now().isBefore(user.getVerificationCodeExpires())) {
            if(input.getVerificationCode().equals(user.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpires(null);

                System.out.println(user.getRole());
                userRepo.save(user);

                return jwtUtils.generateTokenFromUsername(user.getUsername());
            }else {
                throw new VerifyEmailException("Verification code is invalid!");
            }
        }else {
            throw new VerifyEmailException("Verification Code Expired!");
        }

    }

    public AppUser regenetrateVerificationCode(ResendEmailDTO input) throws VerifyEmailException {

        Optional<AppUser> userOptional = userRepo.findByEmail(input.getEmail());

        if(userOptional.isEmpty()) throw new VerifyEmailException("No user with such email!");

        AppUser user = userOptional.get();

        if(user.isEnabled()) throw new VerifyEmailException("User is already verified!");

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpires(LocalDateTime.now().plusMinutes(15));
        return userRepo.save(user);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public void save(AppUser notVerifiedUser) {
        userRepo.save(notVerifiedUser);
    }

    public AppUser saveWithCheck(AppUser notVerifiedUser) throws LoginFormException {
        try{
            return userRepo.save(notVerifiedUser);
        }catch (DataIntegrityViolationException e) {
            throw  new LoginFormException("User Already Exists");
        }
    }
}
