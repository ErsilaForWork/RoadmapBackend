package ers.roadmap.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MyUserDetails implements UserDetails {

    private final String username;
    private String password;
    private List<GrantedAuthority> authorities;

    public MyUserDetails(String username, String password, AppRole role) {
        this.username = username;
        this.password = password;
        this.authorities = List.of(new SimpleGrantedAuthority(role.toString()));
    }

    public static MyUserDetails build(AppUser user) {
        return new MyUserDetails(user.getUsername(), user.getPassword(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
