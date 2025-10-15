package ers.roadmap.security.config;

import ers.roadmap.jwt.JwtFilter;
import ers.roadmap.security.model.AppRole;
import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.model.EnumAppRole;
import ers.roadmap.security.repo.AppRoleRepo;
import ers.roadmap.security.repo.AppUserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppRoleRepo roleRepo;
    private final AppUserRepo userRepo;
    private final JwtFilter jwtFilter;

    public SecurityConfig(AppRoleRepo roleRepo, AppUserRepo userRepo, JwtFilter jwtFilter) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain config(HttpSecurity http) throws Exception {

        http.csrf(customizer -> customizer.disable());
        http.authorizeHttpRequests(request ->
                request.requestMatchers("/security/public/**").permitAll()
                        .requestMatchers("/error**").permitAll()
                        .anyRequest().authenticated());

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CommandLineRunner initData() {

        return line -> {
            if(!roleRepo.existsAppRoleByRole(EnumAppRole.ROLE_USER)){
                roleRepo.save(new AppRole(EnumAppRole.ROLE_USER));
            }

            if(!roleRepo.existsAppRoleByRole(EnumAppRole.ROLE_ADMIN)){
                roleRepo.save(new AppRole(EnumAppRole.ROLE_ADMIN));
            }

            if(!userRepo.existsByUsername("user")) {
                userRepo.save(new AppUser("user","user@gmail.com", passwordEncoder().encode("user1234"), roleRepo.getAppRoleByRole(EnumAppRole.ROLE_USER), true));
            }

            if(!userRepo.existsByUsername("admin")) {
                userRepo.save(new AppUser("admin", "admin@gmail.com", passwordEncoder().encode("admin123"), roleRepo.getAppRoleByRole(EnumAppRole.ROLE_ADMIN), true));
            }
        };

    }
}
