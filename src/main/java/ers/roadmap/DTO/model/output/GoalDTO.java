package ers.roadmap.DTO.model.output;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.roadmap.model.enums.Status;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class GoalDTO {

    private Long goalId;

    private Status status;

    private Integer completedPercent;

    @NotBlank
    private String title;

    @JsonBackReference
    private RoadmapDTO roadmap;

    @JsonManagedReference
    private List<ActionDTO> actions;


    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
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

    public RoadmapDTO getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(RoadmapDTO roadmap) {
        this.roadmap = roadmap;
    }

    public Integer getCompletedPercent() {
        return completedPercent;
    }

    public void setCompletedPercent(Integer completedPercent) {
        this.completedPercent = completedPercent;
    }

    public List<ActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<ActionDTO> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return "GoalDTO{" +
                "goalId=" + goalId +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", actions=" + actions +
                '}';
    }
}
