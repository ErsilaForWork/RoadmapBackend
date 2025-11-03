package ers.roadmap.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ers.roadmap.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Entity
@Table(
        name = "actions"
)
@SequenceGenerator(
        name = "actionSeq",
        sequenceName = "actionDemoSeq",
        allocationSize = 1
)
public class Action {


    @Id
    @Column(name = "action_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actionSeq")
    private Long actionId;

    @NotBlank
    @Column(nullable = false)
    private String title;

    //Explanation of action
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NOT_COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", referencedColumnName = "goal_id")
    @JsonBackReference
    private Goal goal;

    private Long position;

    @Override
    public boolean equals(Object o) {

        if(o == this) return true;

        if(!(o instanceof Action other)) return false;

        if(other.getActionId() != null && this.actionId != null) {
            return Objects.equals(other.getActionId(), this.actionId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(actionId);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }
}
