package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.model.input.RoadmapInput;
import ers.roadmap.DTO.model.output.AppUserDTOForRoadmap;
import ers.roadmap.DTO.model.output.RoadmapDTO;
import ers.roadmap.model.Roadmap;
import org.springframework.stereotype.Component;

@Component
public class RoadmapMapper {

    public Roadmap toRoadmap(RoadmapInput roadmapDTO) {

        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(roadmapDTO.getTitle());
        roadmap.setGoals(roadmapDTO.getGoals().stream().map(GoalMapper::toGoal).toList());

        return roadmap;
    }

    public RoadmapDTO toDTO(Roadmap roadmap) {
        RoadmapDTO roadmapDTO = new RoadmapDTO();
        roadmapDTO.setId(roadmap.getRoadmapId());
        roadmapDTO.setCompletedPercent(roadmap.getCompletedPercent());
        roadmapDTO.setTitle(roadmap.getTitle());
        roadmapDTO.setStatus(roadmap.getStatus());
        roadmapDTO.setOwner(new AppUserDTOForRoadmap(roadmap.getOwner().getUserId(), roadmap.getOwner().getUsername(), roadmap.getOwner().getRole()));
        roadmapDTO.setGoals(roadmap.getGoals().stream().map(GoalMapper::toDTO).toList());
        return roadmapDTO;
    }

}
