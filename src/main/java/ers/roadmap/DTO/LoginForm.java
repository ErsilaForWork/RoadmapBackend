package ers.roadmap.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginForm {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username may contain only Latin letters, digits and underscore")
    @Size(max = 256)
    private String username;

    @NotBlank
    @Size(min = 8, max = 256)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
