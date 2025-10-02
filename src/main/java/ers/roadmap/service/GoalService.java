package ers.roadmap.service;

import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.repo.GoalRepo;
import ers.roadmap.repo.RoadmapRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {

    private final RoadmapRepo roadmapRepo;
    private final RoadmapService roadmapService;
    private final GoalRepo goalRepo;
    private final Validator validator;

    public GoalService(RoadmapRepo roadmapRepo, RoadmapService roadmapService, GoalRepo goalRepo, Validator validator) {
        this.roadmapRepo = roadmapRepo;
        this.roadmapService = roadmapService;
        this.goalRepo = goalRepo;
        this.validator = validator;
    }

    public void setGoalCompleted(Goal goal, Roadmap roadmap) {
        //If our goal is the last one of this Roadmap
        if(goal.equals(roadmap.findLastGoal())) {
            goal.setStatus(Status.COMPLETED);
            roadmapService.setRoadmapCompleted(roadmap);
        }else {
            int index = roadmap.getGoals().indexOf(goal);
            goal.setStatus(Status.COMPLETED);
            Goal nextGoal = roadmap.getGoals().get(index + 1);
            nextGoal.setNowWorkingAction(nextGoal.findFirstAction());
            roadmap.setNowWorkingGoal(nextGoal);
        }

    }

    public void setCalculatedPercent(Goal goal, Roadmap roadmap) {
        int percent = calculatePercent(goal);
        if (percent >= 100) {
            roadmapService.setCalculatedPercent(roadmap);
        }
        goal.setCompletedPercent(percent);
    }

    private int calculatePercent(Goal goal) {
        int completedQuantity = goal.getActions()
                .stream()
                .filter(a -> a.getStatus() == Status.COMPLETED)
                .toList().size();

        int allActions = goal.getActions().size();

        return Math.round((float) (completedQuantity*100) / (float) (allActions));
    }


    public Goal validateToComplete(Long goalId) {

        Goal goal = goalRepo.findById(goalId).orElseThrow();
        validator.validateGoalComplete(goal);
        return goal;

    }

    public List<Goal> findGoalsWithActionsByRoadmapId(Long roadmapId) {
        return goalRepo.findGoalsWithActionsByRoadmapIdGraph(roadmapId);
    }

    public Goal findGoalWithActionsByActionId(Long actionId) {
        return goalRepo.findGoalWithActionsByActionIdGraph(actionId).get();
    }
}
