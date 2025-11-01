package ers.roadmap.DTO.patch.mapper;

import ers.roadmap.DTO.patch.PatchGoalDTO;
import ers.roadmap.model.Goal;
import org.springframework.stereotype.Component;

@Component
public class PatchGoalMapper {

    public void merge(Goal goal, PatchGoalDTO goalDTO) {
        if(goalDTO.getTitle() != null) {
            if(!goalDTO.getTitle().isEmpty())
                goal.setTitle(goalDTO.getTitle());
        }
    }

}
