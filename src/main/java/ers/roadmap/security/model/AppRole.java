package ers.roadmap.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ers.roadmap.security.repo.AppRoleRepo;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "app_roles"
)
@SequenceGenerator(
        name = "role_seq_gen",
        sequenceName = "role_seq",
        allocationSize = 1
)
public class AppRole {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq_gen")
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EnumAppRole role;

    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<AppUser> users = new HashSet<>();

    public AppRole() {}

    public AppRole(EnumAppRole enumAppRole) {
        role = enumAppRole;
    }

    @Override
    public String toString() {

        if(role == EnumAppRole.ROLE_USER) {
            return "ROLE_USER";
        } else  {
            return "ROLE_ADMIN";
        }

    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public EnumAppRole getRole() {
        return role;
    }

    public void setRole(EnumAppRole role) {
        this.role = role;
    }

    public Set<AppUser> getUsers() {
        return users;
    }

    public void setUsers(Set<AppUser> users) {
        this.users = users;
    }
}