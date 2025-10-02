package ers.roadmap.security.service;

import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.model.MyUserDetails;
import ers.roadmap.security.repo.AppUserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final AppUserRepo userRepo;

    public MyUserDetailsService(AppUserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return MyUserDetails.build(userRepo.findAppUserByUsername(username).get());
        }catch (NoSuchElementException e){
            throw new UsernameNotFoundException("Username not found!");
        }
    }
}
