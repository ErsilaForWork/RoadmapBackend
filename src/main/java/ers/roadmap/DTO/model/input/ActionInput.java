package ers.roadmap.DTO.model.input;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ers.roadmap.DTO.model.output.GoalDTO;
import jakarta.validation.constraints.NotBlank;

public class ActionInput {

    @NotBlank
    private String title;

    private String description;

    @JsonBackReference
    private GoalInput goal;


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

    public GoalInput getGoal() {
        return goal;
    }

    public void setGoal(GoalInput goal) {
        this.goal = goal;
    }
}
