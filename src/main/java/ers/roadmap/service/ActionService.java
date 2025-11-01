package ers.roadmap.service;

import ers.roadmap.DTO.mappers.GoalMapper;
import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.DTO.patch.PatchActionDTO;
import ers.roadmap.DTO.patch.mapper.PatchActionMapper;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.repo.ActionRepo;
import ers.roadmap.repo.GoalRepo;
import ers.roadmap.repo.RoadmapRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ActionService {

    private final ActionRepo actionRepo;
    private final Validator validator;
    private final GoalService goalService;
    private final PatchActionMapper actionMapper;

    public ActionService(ActionRepo actionRepo, Validator validator, GoalService goalService, PatchActionMapper actionMapper) {
        this.actionRepo = actionRepo;
        this.validator = validator;
        this.goalService = goalService;
        this.actionMapper = actionMapper;
    }

    public Action validateToComplete(Long actionId) throws NoSuchElementException, ValidationException {
        return validator.validateActionComplete(actionId);
    }

    public boolean isOwner(String username, Long actionId) {

        Action action;

        try{
            System.out.println("Queries to get action----------------------------");
            action = actionRepo.findWithGoalAndRoadmap(actionId).get();
            System.out.println("END to get action----------------------------");

        }catch (NoSuchElementException e) {
            return true;
        }

        return action.getGoal().getRoadmap().getOwner().getUsername().equals(username);
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

    public void partialUpdate(Long actionId, @Valid PatchActionDTO actionDTO) throws NoSuchElementException, ConstraintsNotMetException{

        Optional<Action> optionalAction = actionRepo.findById(actionId);

        if(optionalAction.isEmpty()) throw new NoSuchElementException("No such action with that id!");

        Action action = optionalAction.get();

        try{
            actionMapper.merge(action, actionDTO);

            actionRepo.save(action);

        }catch (DataIntegrityViolationException e) {
            throw new ConstraintsNotMetException("Unable to save the entity!");
        }

    }
}
