package ers.roadmap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.roadmap.model.enums.Status;
import ers.roadmap.security.model.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "roadmaps"
)
@SequenceGenerator(
        name = "roadmapSeq",
        sequenceName = "roadmapDemoSeq",
        allocationSize = 1
)
@NamedEntityGraph(
        name = "roadmap_with_goals",
        attributeNodes = {
                @NamedAttributeNode("goals"),
                @NamedAttributeNode("nowWorkingGoal"),
                @NamedAttributeNode("owner")
        }
)
@NamedEntityGraph(
        name = "roadmap_with_owner",
        attributeNodes = {
                @NamedAttributeNode("owner")
        }
)
public class Roadmap {

    @Id
    @Column(name = "roadmap_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roadmapSeq")
    private Long roadmapId;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(name = "percent_complete")
    private Integer completedPercent = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NOT_COMPLETED;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "working_id", referencedColumnName = "goal_id")
    @JsonIgnore
    private Goal nowWorkingGoal;

    @JsonManagedReference
    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<Goal> goals;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    @JsonIgnoreProperties({"password","email","roadmaps","creationTime","role","phoneNumber"})
    private AppUser owner;

    @PrePersist
    @PreUpdate
    private void validate() {

        if(this.status == Status.COMPLETED)
            completedPercent = 100;

        if(completedPercent < 0)
            completedPercent = 0;
        if(completedPercent > 100)
            completedPercent = 100;

    }

    public Roadmap() {
        goals = new ArrayList<>();
    }


    @Override
    public boolean equals(Object o) {

        if(o == this) return true;

        if (!(o instanceof Roadmap roadmap)) return false;

        if(roadmap.roadmapId != null && this.roadmapId != null) {
            return Objects.equals(roadmapId, roadmap.roadmapId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roadmapId);
    }

    public void addGoal(Goal goal) {
        if (goal == null) return;
        if (this.goals == null) this.goals = new ArrayList<>();

        goal.setRoadmap(this);
        this.goals.add(goal);
    }


    public Goal getNowWorkingGoal() {
        return nowWorkingGoal;
    }

    public void setNowWorkingGoal(Goal nowWorkingGoal) {
        if(nowWorkingGoal != null)
            nowWorkingGoal.setStatus(Status.NOW_WORKING);
        this.nowWorkingGoal = nowWorkingGoal;
    }

    public void removeGoal(Goal goal) {
        if (goal == null || this.goals == null) return;
        if (this.goals.remove(goal)) {
            if (goal.getRoadmap() == this) {
                goal.setNowWorkingAction(null);
                goal.setRoadmap(null);
            }
        }
    }

    public Integer getCompletedPercent() {
        return completedPercent;
    }

    public void setCompletedPercent(Integer completedPercent) {
        this.completedPercent = completedPercent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getRoadmapId() {
        return roadmapId;
    }

    public void setRoadmapId(Long roadmapId) {
        this.roadmapId = roadmapId;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    // setter копирует элементы в внутреннюю коллекцию и поддерживает связь
    public void setGoals(List<Goal> goals) {
        if (this.goals == null) {
            this.goals = new ArrayList<>();
        } else {
            this.goals.clear();
        }

        for (Goal g : goals) {
            g.setRoadmap(this);   // сохраняем связь
            this.goals.add(g);
        }
    }

    public Goal findLastGoal() {
        return goals.getLast();
    }
}
