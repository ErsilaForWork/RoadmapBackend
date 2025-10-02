package ers.roadmap.DTO.model.input;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.roadmap.DTO.model.output.ActionDTO;
import ers.roadmap.DTO.model.output.RoadmapDTO;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class GoalInput {

    @NotBlank
    private String title;

    @JsonBackReference
    private RoadmapInput roadmap;

    @JsonManagedReference
    private List<ActionInput> actions;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RoadmapInput getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(RoadmapInput roadmap) {
        this.roadmap = roadmap;
    }

    public List<ActionInput> getActions() {
        return actions;
    }

    public void setActions(List<ActionInput> actions) {
        this.actions = actions;
    }
}
