package ers.roadmap.service;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.DTO.patch.PatchGoalDTO;
import ers.roadmap.DTO.patch.PatchPositionDTO;
import ers.roadmap.DTO.patch.mapper.PatchGoalMapper;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.exceptions.UnableToMoveExeption;
import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.repo.GoalRepo;
import ers.roadmap.repo.RoadmapRepo;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class GoalService {

    private final RoadmapService roadmapService;
    private final GoalRepo goalRepo;
    private final Validator validator;
    private final PatchGoalMapper goalMapper;
    private final RoadmapRepo roadmapRepo;

    @PersistenceContext
    private EntityManager entityManager;

    public GoalService(RoadmapService roadmapService, GoalRepo goalRepo, Validator validator, PatchGoalMapper goalMapper, RoadmapRepo roadmapRepo) {
        this.roadmapService = roadmapService;
        this.goalRepo = goalRepo;
        this.validator = validator;
        this.goalMapper = goalMapper;
        this.roadmapRepo = roadmapRepo;
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

    public boolean isOwner(String username, Long goalId) {
        return goalRepo.existsByGoalIdAndRoadmap_Owner_Username(goalId, username);
    }

    public void partialUpdate(Long goalId, PatchGoalDTO goalDTO) throws ConstraintsNotMetException {

        Optional<Goal> optionalGoal = goalRepo.findById(goalId);

        if(optionalGoal.isEmpty()) throw new NoSuchElementException("No such goals with this goalId");

        Goal goal = optionalGoal.get();

        goalMapper.merge(goal, goalDTO);

        try{
            goalRepo.save(goal);
        }catch (DataIntegrityViolationException e) {
            throw new ConstraintsNotMetException("Unable to save the entity");
        }
    }

    //Checks if the previous Action and NextAction are completed
    public void validateToMove(Long actionId, PatchPositionDTO goalPositionDTO) throws NoSuchElementException, UnableToMoveExeption {

        // If the moving action is not exists
        if(!goalRepo.existsById(actionId)) throw new NoSuchElementException("No such action with id: " + actionId);

        //If the moving action is already completed
        if(goalRepo.existsByGoalIdAndStatus(actionId, Status.COMPLETED)) throw new UnableToMoveExeption("Unable to move completed action!");

        //If the JSON is empty
        if(goalPositionDTO.getPrevId() == null && goalPositionDTO.getNextId() == null) throw new NoSuchElementException("Both previous and next ids are empty!");

        //If user wants to move action to the beginning
        if(goalPositionDTO.getPrevId() == null) {

            if(!goalRepo.existsById(goalPositionDTO.getNextId())) throw new NoSuchElementException("No such action with id: " + goalPositionDTO.getNextId());

            if(goalRepo.existsByGoalIdAndStatus(goalPositionDTO.getNextId(), Status.COMPLETED)) {
                throw new UnableToMoveExeption("Unable to move the beginning, next action is already completed!");
            }
        }
        //If user wants to move action to the end
        else if(goalPositionDTO.getNextId() == null) {
            if(!goalRepo.existsById(goalPositionDTO.getPrevId())) throw new NoSuchElementException("No such action with id: " + goalPositionDTO.getPrevId());
        }
        //If user wants to move action to the middle
        else {
            if(!goalRepo.existsById(goalPositionDTO.getNextId()) || !goalRepo.existsById(goalPositionDTO.getPrevId())) throw new NoSuchElementException("No such action with id: " + goalPositionDTO.getNextId() + " or " + goalPositionDTO.getPrevId());

            //If the status of nextId is Completed
            if(goalRepo.existsByGoalIdAndStatus(goalPositionDTO.getNextId(), Status.COMPLETED)) {
                throw new UnableToMoveExeption("Unable to move because it is in the block of completed tasks!");
            }

        }
    }

    //This method must come only after validateToMove() method!!!!!!!!!!!!!!!!!!!!!!
    @Transactional
    public void move(Long goalId, PatchPositionDTO dto) {
        // 1. Lock moving goal
        Goal moving = goalRepo.findByIdForUpdate(goalId).get();

        Long roadmapId = moving.getRoadmap().getRoadmapId();

        Goal prev = null;
        Goal next = null;

        if (dto.getPrevId() != null) {
            prev = goalRepo.findByIdForUpdate(dto.getPrevId()).get();
        }
        if (dto.getNextId() != null) {
            next = goalRepo.findByIdForUpdate(dto.getNextId()).get();
        }

        if(moving.getStatus() == Status.NOW_WORKING) {
            setNowWorkingToNextGoal(moving);
        }

        long newPos;

        // moving to beginning
        if (prev == null && next != null) {
            newPos = next.getPosition() / 2;
            if (newPos == next.getPosition() || newPos == 0) {
                goalRepo.reindexGoalsByRoadmap(roadmapId, RoadmapMapper.POSITION_STEP);
                try{
                    entityManager.refresh(next);
                }catch (Exception ignore) {}
                next = goalRepo.findByIdForUpdate(dto.getNextId()).get();
                newPos = next.getPosition() / 2;
            }
            setNowWorkingToMoving(moving, next);
        }
        // moving to end
        else if (next == null && prev != null) {
            newPos = prev.getPosition() + RoadmapMapper.POSITION_STEP;
        }
        // moving to middle
        else if (prev != null) {
            long gap = next.getPosition() - prev.getPosition();
            if (gap <= 1) {
                goalRepo.reindexGoalsByRoadmap(roadmapId, RoadmapMapper.POSITION_STEP);
                try{
                    entityManager.refresh(next);
                }catch (Exception ignore) {}
                try{
                    entityManager.refresh(prev);
                }catch (Exception ignore) {}
                prev = goalRepo.findByIdForUpdate(dto.getPrevId()).get();
                next = goalRepo.findByIdForUpdate(dto.getNextId()).get();
                gap = next.getPosition() - prev.getPosition();
                if (gap <= 1) {
                    newPos = prev.getPosition() + 1;
                } else {
                    newPos = prev.getPosition() + gap / 2;
                }
            } else {
                newPos = prev.getPosition() + gap / 2;
            }

            if(prev.getStatus() == Status.COMPLETED) {
                setNowWorkingToMoving(moving, next);
            }

        } else {
            // no prev and next — single element
            newPos = RoadmapMapper.POSITION_STEP;
        }

        moving.setPosition(newPos);
        goalRepo.save(moving);
    }

    //Our goal has status nowWorking
    private void setNowWorkingToNextGoal(Goal goal) throws NoSuchElementException {
        Roadmap roadmap = goal.getRoadmap();
        int index = roadmap.getGoals().indexOf(goal);
        if(index == -1) throw new NoSuchElementException("There is no such goal in the roadmap");

        //It is guarantied that index + 1 < size(), because of the method validateToMove() method
        Goal nextGoal = roadmap.getGoals().get(index + 1);

        goal.setStatus(Status.NOT_COMPLETED);
        goal.getNowWorkingAction().setStatus(Status.NOT_COMPLETED);
        goal.setNowWorkingAction(null);

        nextGoal.setStatus(Status.NOW_WORKING);
        nextGoal.setNowWorkingAction(getFirstNotCompleteAction(nextGoal));
        nextGoal.getNowWorkingAction().setStatus(Status.NOW_WORKING);
        roadmap.setNowWorkingGoal(nextGoal);
        roadmapRepo.save(roadmap);
    }

    private void setNowWorkingToMoving(Goal moving, Goal next) {
        Roadmap roadmap = moving.getRoadmap();
        moving.setStatus(Status.NOW_WORKING);
        if (next != null) {
            next.setStatus(Status.NOT_COMPLETED);
            next.getNowWorkingAction().setStatus(Status.NOT_COMPLETED);
            next.setNowWorkingAction(null);
        }
        if (moving.getNowWorkingAction() == null && !moving.getActions().isEmpty()) {
            moving.setNowWorkingAction(getFirstNotCompleteAction(moving));
        }
        roadmap.setNowWorkingGoal(moving);
        roadmapRepo.save(roadmap);
    }

    private Action getFirstNotCompleteAction(Goal goal) {
        for (Action action : goal.getActions()) {
            if(action.getStatus() == Status.NOT_COMPLETED) {
                return action;
            }
        }
        return null;
    }
}
