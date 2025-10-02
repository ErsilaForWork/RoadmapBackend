package ers.roadmap.service;

import ers.roadmap.DTO.mappers.GoalMapper;
import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.repo.ActionRepo;
import ers.roadmap.repo.GoalRepo;
import ers.roadmap.repo.RoadmapRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ActionService {

    private final ActionRepo actionRepo;
    private final Validator validator;
    private final GoalService goalService;
    private final RoadmapMapper roadmapMapper;
    private final GoalRepo goalRepo;
    private final RoadmapRepo roadmapRepo;

    public ActionService(ActionRepo actionRepo, Validator validator, GoalService goalService, RoadmapMapper roadmapMapper, GoalMapper goalMapper, GoalRepo goalRepo, RoadmapRepo roadmapRepo) {
        this.actionRepo = actionRepo;
        this.validator = validator;
        this.goalService = goalService;
        this.roadmapMapper = roadmapMapper;
        this.goalRepo = goalRepo;
        this.roadmapRepo = roadmapRepo;
    }

    public Action validateToComplete(Long actionId) throws NoSuchElementException, ValidationException {
        return validator.validateActionComplete(actionId);
    }

    @Transactional
    public void setActionCompleted(Action action, Goal goal, Roadmap roadmap) {
        //If our action is the last one in this goal
        if(action.equals(goal.findLastAction())) {
            action.setStatus(Status.COMPLETED);
            goalService.setGoalCompleted(goal, roadmap);
        }else {
            int index = goal.getActions().indexOf(action);
            Action nextAction = goal.getActions().get(index + 1);
            action.setStatus(Status.COMPLETED);
            goal.setNowWorkingAction(nextAction);
        }
    }
}
