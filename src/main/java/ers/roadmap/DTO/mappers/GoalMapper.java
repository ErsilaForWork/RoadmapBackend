package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.model.input.GoalInput;
import ers.roadmap.DTO.model.output.GoalDTO;
import ers.roadmap.model.Goal;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public static Goal toGoal(GoalInput goalDTO) {

        Goal goal = new Goal();
        goal.setTitle(goalDTO.getTitle());
        goal.setActions(goalDTO.getActions().stream().map(ActionMapper::toAction).toList());

        return goal;

    }

    public static GoalDTO toDTO(Goal goal) {

        GoalDTO goalDTO = new GoalDTO();
        goalDTO.setGoalId(goal.getGoalId());
        goalDTO.setStatus(goal.getStatus());
        goalDTO.setTitle(goal.getTitle());
        goalDTO.setCompletedPercent(goal.getCompletedPercent());
        goalDTO.setPosition(goal.getPosition());
        goalDTO.setActions(goal.getActions().stream().map(ActionMapper::toDTO).toList());

        return goalDTO;

    }

}
