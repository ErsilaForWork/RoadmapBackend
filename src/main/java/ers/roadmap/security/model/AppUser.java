package ers.roadmap.security.model;

import ers.roadmap.DTO.RegistrationForm;
import ers.roadmap.model.Roadmap;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "users"
)
@SequenceGenerator(
        name = "user_seq_gen",
        sequenceName = "user_seq",
        allocationSize = 1
)
@NamedEntityGraph(
        name = "user_with_roadmaps",
        attributeNodes = {
                @NamedAttributeNode("roadmaps"),
                @NamedAttributeNode("role")
        }
)
@NamedEntityGraph(
        name = "user_with_role",
        attributeNodes = {
                @NamedAttributeNode("role")
        }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, updatable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "creation_time", updatable = false)
    private LocalDateTime creationTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    private AppRole role;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "owner")
    private List<Roadmap> roadmaps;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "streak")
    private Integer streak;

    @Column(name = "streak_broken")
    private boolean streakBroken;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    private String verificationCode;

    private LocalDateTime verificationCodeExpires;

    private boolean enabled;

    public AppUser() {
        lastActivityDate = LocalDate.now();
        streak = 1;
        streakBroken = false;
        lastLoginDate = LocalDate.now();
    }

    public AppUser(String username, String email, String password, AppRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        roadmaps = new ArrayList<>();
        lastActivityDate = LocalDate.now();
        streak = 1;
        streakBroken = false;
        lastLoginDate = LocalDate.now();
    }

    public AppUser(String username, String email, String password, AppRole role, boolean enabled) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        roadmaps = new ArrayList<>();
        lastActivityDate = LocalDate.now();
        streak = 1;
        streakBroken = false;
        lastLoginDate = LocalDate.now();
    }

    public AppUser(RegistrationForm registrationForm, AppRole role) {
        this.username = registrationForm.getUsername();
        this.email = registrationForm.getEmail();
        this.password = registrationForm.getPassword();
        this.role = role;
        roadmaps = new ArrayList<>();
        lastActivityDate = LocalDate.now();
        streak = 1;
        streakBroken = false;
        lastLoginDate = LocalDate.now();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;

        if(!(o instanceof AppUser other)) return false;

        if(other == this) return true;

        if(other.userId != null && this.userId != null) {
            return Objects.equals(other.username, this.username);
        }
        return false;
    }

    public List<Roadmap> getRoadmaps() {
        return roadmaps;
    }

    public void setRoadmaps(List<Roadmap> roadmaps) {
        this.roadmaps = roadmaps;
    }

    public void addRoadmap(Roadmap roadmap) {

        if(roadmap == null) {
            return;
        }

        roadmaps.add(roadmap);
        roadmap.setOwner(this);
    }

    public boolean removeRoadmap(Roadmap roadmap) {

        boolean removed = roadmaps.remove(roadmap);

        if (removed) {
            roadmap.setOwner(null);
        }

        return removed;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public AppRole getRole() {
        return role;
    }

    public void setRole(AppRole role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpires() {
        return verificationCodeExpires;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public boolean isStreakBroken() {
        return streakBroken;
    }

    public void setStreakBroken(boolean streakBroken) {
        this.streakBroken = streakBroken;
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public void setVerificationCodeExpires(LocalDateTime verificationCodeExpires) {
        this.verificationCodeExpires = verificationCodeExpires;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}