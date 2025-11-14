package ers.roadmap.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.roadmap.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "goals"
)
@SequenceGenerator(
        name = "goalSeq",
        sequenceName = "goalDemoSeq",
        allocationSize = 1
)
@NamedEntityGraph(
        name = "goal_with_actions",
        attributeNodes = {
                @NamedAttributeNode("actions"),
                @NamedAttributeNode("nowWorkingAction"),
                @NamedAttributeNode("roadmap")
        }
)
public class Goal {

    public Goal() {
        actions = new ArrayList<>();
    }

    @Id
    @Column(name = "goal_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goalSeq")
    private Long goalId;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(name = "percent_complete")
    private Integer completedPercent = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NOT_COMPLETED;

    @OneToOne
    @JoinColumn(name = "working_id", referencedColumnName = "action_id")
    @JsonIgnore
    private Action nowWorkingAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", referencedColumnName = "roadmap_id")
    @JsonBackReference
    private Roadmap roadmap;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @JsonManagedReference
    private List<Action> actions;

    private Long position;

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

    @Override
    public boolean equals(Object o) {

        if(o == this) return true;

        if (!(o instanceof Goal goal)) return false;

        if(goal.getGoalId() != null && this.goalId != null) {
            return Objects.equals(this.goalId, goal.getGoalId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(goalId);
    }

    public Action getNowWorkingAction() {
        return nowWorkingAction;
    }

    public void setNowWorkingAction(Action nowWorkingAction) {
        if(nowWorkingAction != null)
            nowWorkingAction.setStatus(Status.NOW_WORKING);
        this.nowWorkingAction = nowWorkingAction;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public void setRoadmap(Roadmap roadmap) {
        this.roadmap = roadmap;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void addAction(Action action) {
        if(action == null)
            return;

        actions.add(action);
        action.setGoal(this);
    }

    public boolean removeAction(Action action) {
        boolean removed = actions.remove(action);

        if(removed) {
            action.setGoal(null);
        }

        return removed;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions.stream()
                .peek(a -> a.setGoal(this))
                .toList();
    }

    public Action findLastAction() {
        return actions.getLast();
    }

    public Action findFirstAction() {
        return actions.getFirst();
    }

    public Action findFirstNotCompletedAction() {

        for (Action action : actions) {
            if(action.getStatus() == Status.NOT_COMPLETED) return action;
        }

        return null;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "roadmap=" + roadmap +
                ", nowWorkingAction=" + nowWorkingAction +
                ", status=" + status +
                ", completedPercent=" + completedPercent +
                ", title='" + title + '\'' +
                ", goalId=" + goalId +
                '}';
    }
}
