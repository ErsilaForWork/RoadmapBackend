package ers.roadmap.DTO.model.output;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ers.roadmap.model.enums.Status;
import jakarta.validation.constraints.NotBlank;

public class ActionDTO {

    private Long actionId;

    private Status status;

    @NotBlank
    private String title;

    private String description;

    @JsonBackReference
    private GoalDTO goal;

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
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

    public GoalDTO getGoal() {
        return goal;
    }

    public void setGoal(GoalDTO goal) {
        this.goal = goal;
    }

    @Override
    public String toString() {
        return "ActionDTO{" +
                "actionId=" + actionId +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
