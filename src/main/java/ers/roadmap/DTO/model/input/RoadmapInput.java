package ers.roadmap.DTO.model.input;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoadmapInput {

    @NotBlank
    private String title;

    @JsonManagedReference
    private List<GoalInput> goals;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<GoalInput> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalInput> goals) {
        this.goals = goals;
    }
}
