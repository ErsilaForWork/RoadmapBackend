package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.model.input.RoadmapInput;
import ers.roadmap.DTO.model.output.AppUserDTOForRoadmap;
import ers.roadmap.DTO.model.output.RoadmapDTO;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import org.springframework.stereotype.Component;

@Component
public class RoadmapMapper {

    public static final Long POSITION_STEP = 1L;

    public Roadmap toRoadmap(RoadmapInput roadmapDTO) {

        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(roadmapDTO.getTitle());
        roadmap.setGoals(roadmapDTO.getGoals().stream().map(GoalMapper::toGoal).toList());
        setPositions(roadmap);
        setNowWorkings(roadmap);


        return roadmap;
    }

    private void setNowWorkings(Roadmap roadmap) {
        if(roadmap.getNowWorkingGoal() == null) {
            Goal nowWorkingGoal = roadmap.getGoals().getFirst();
            if(nowWorkingGoal.getNowWorkingAction() == null) {
                nowWorkingGoal.setNowWorkingAction(nowWorkingGoal.getActions().getFirst());
            }
            roadmap.setNowWorkingGoal(nowWorkingGoal);
        }
    }

    private void setPositions(Roadmap roadmap) {
        if(roadmap.getGoals() != null) {
            //Setting positions of Goals (outer loop) and Actions (inner loop)
            for (int i = 0; i < roadmap.getGoals().size(); i++) {
                Goal current = roadmap.getGoals().get(i);
                current.setPosition((i+1) * POSITION_STEP);

                if(current.getActions() != null) {
                    for (int j = 0; j < current.getActions().size(); j++) {
                        current.getActions().get(j).setPosition((j+1) * POSITION_STEP);
                    }
                }
            }
        }
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
