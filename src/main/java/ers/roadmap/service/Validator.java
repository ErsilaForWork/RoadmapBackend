package ers.roadmap.service;

import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.repo.ActionRepo;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class Validator {

    private final ActionRepo actionRepo;

    public Validator(ActionRepo actionRepo) {
        this.actionRepo = actionRepo;
    }

    public Action validateActionComplete(Long actionId) throws NoSuchElementException, ValidationException {

        Action action = actionRepo.findWithGoalAndRoadmap(actionId).orElseThrow(
                () -> new NoSuchElementException("No such action")
        );

        Goal goal = action.getGoal();

        if (!action.equals(goal.getNowWorkingAction())) {
            throw new  ValidationException("Action is not now working");
        }

        return action;
    }

    public void validateGoalComplete(Goal goal) {

        Roadmap roadmap = goal.getRoadmap();
        if(!goal.equals(roadmap.getNowWorkingGoal())) {
            throw new  ValidationException("Goal is not now working");
        }

    }
}
