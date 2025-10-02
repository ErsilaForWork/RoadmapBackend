package ers.roadmap.DTO.model.output;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.roadmap.model.enums.Status;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class RoadmapDTO {

    private Long id;

    private Integer completedPercent;

    @NotBlank
    private String title;

    @JsonManagedReference
    private List<GoalDTO> goals;

    private Status status;

    private AppUserDTOForRoadmap owner;

    public AppUserDTOForRoadmap getOwner() {
        return owner;
    }

    public void setOwner(AppUserDTOForRoadmap owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<GoalDTO> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalDTO> goals) {
        this.goals = goals;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCompletedPercent() {
        return completedPercent;
    }

    public void setCompletedPercent(Integer completedPercent) {
        this.completedPercent = completedPercent;
    }

    @Override
    public String toString() {
        return "RoadmapDTO{" + "\n" +
                "id=" + id + "\n" +
                "title='" + title + '\'' + "\n" +
                "goals=" + goals + "\n" +
                "status=" + status + "\n" +
                '}';
    }
}
