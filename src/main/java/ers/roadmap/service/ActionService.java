package ers.roadmap.service;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.DTO.patch.PatchActionDTO;
import ers.roadmap.DTO.patch.PatchPositionDTO;
import ers.roadmap.DTO.patch.mapper.PatchActionMapper;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.exceptions.UnableToMoveExeption;
import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.repo.ActionRepo;
import ers.roadmap.repo.GoalRepo;
import ers.roadmap.repo.RoadmapRepo;
import jakarta.persistence.EntityManager;
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
    private final GoalRepo goalRepo;
    private final EntityManager entityManager;
    private final RoadmapRepo roadmapRepo;

    public ActionService(ActionRepo actionRepo, Validator validator, GoalService goalService, PatchActionMapper actionMapper, GoalRepo goalRepo, EntityManager entityManager, RoadmapRepo roadmapRepo) {
        this.actionRepo = actionRepo;
        this.validator = validator;
        this.goalService = goalService;
        this.actionMapper = actionMapper;
        this.goalRepo = goalRepo;
        this.entityManager = entityManager;
        this.roadmapRepo = roadmapRepo;
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

    //This method must come only after validateToMove() method!!!!!!!!!!!!!!!!!!!!!!
    @Transactional
    public void move(Long actionId, PatchPositionDTO dto) {
        // 1. Захватить movingAction с блокировкой
        Action moving = actionRepo.findByIdForUpdate(actionId).get();

        Long goalId = moving.getGoal().getGoalId();

        Action prev = null;
        Action next = null;

        if (dto.getPrevId() != null) {
            prev = actionRepo.findByIdForUpdate(dto.getPrevId()).get();
        }
        if (dto.getNextId() != null) {
            next = actionRepo.findByIdForUpdate(dto.getNextId()).get();
        }

        if(moving.getStatus() == Status.NOW_WORKING) {
            setNowWorkingToNextAction(moving);
        }

        long newPos;

        // moving to beginning
        if (prev == null && next != null) {
            // попытка взять середину: next.position / 2
            newPos = next.getPosition() / 2;
            if (newPos == next.getPosition() || newPos == 0) {
                // gap недостаточен -> реиндекс и перерасчёт
                actionRepo.reindexActionsByGoal(goalId, RoadmapMapper.POSITION_STEP);
                entityManager.refresh(next);
                next = actionRepo.findByIdForUpdate(dto.getNextId()).get();
                newPos = next.getPosition() / 2;
            }
            setNowWorkingToMoving(moving, next);
        }
        // moving to end
        else if (next == null && prev != null) {
            newPos = prev.getPosition() + RoadmapMapper.POSITION_STEP;
            // в конце обычно не нужен reindex
        }
        // moving to middle
        else if (prev != null && next != null) {
            long gap = next.getPosition() - prev.getPosition();
            if (gap <= 1) {
                System.out.println("Gap is: " + gap);
                System.out.println("Before reindex: ");
                System.out.println("Prev position: " + prev.getPosition());
                System.out.println("Next position: " + next.getPosition());

                // недостаточный gap -> реиндекс, затем перезагрузить соседей
                actionRepo.reindexActionsByGoal(goalId, RoadmapMapper.POSITION_STEP);
                entityManager.refresh(next);
                entityManager.refresh(prev);
                prev = actionRepo.findByIdForUpdate(dto.getPrevId()).get();
                next = actionRepo.findByIdForUpdate(dto.getNextId()).get();
                gap = next.getPosition() - prev.getPosition();

                System.out.println("After reindex: ");
                System.out.println("Gap is: " + gap);
                System.out.println("Prev position: " + prev.getPosition());
                System.out.println("Next position: " + next.getPosition());

                // после ребаланса gap должен быть >= POSITION_STEP, но на всякий случай:
                if (gap <= 1) {
                    // безопасный fallback: поставить сразу после prev
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
            // нет prev и next — единственный элемент в списке
            newPos = RoadmapMapper.POSITION_STEP;
        }

        System.out.println("Actions new position is: " + newPos);

        // Установить позицию и сохранить
        moving.setPosition(newPos);
        actionRepo.save(moving);
    }

    //Checks if the previous Action and NextAction are completed
    public void validateToMove(Long actionId, PatchPositionDTO actionPositionDTO) throws NoSuchElementException, UnableToMoveExeption {

        // If the moving action is not exists
        if(!actionRepo.existsById(actionId)) throw new NoSuchElementException("No such action with id: " + actionId);

        //If the moving action is already completed
        if(actionRepo.existsByActionIdAndStatus(actionId, Status.COMPLETED)) throw new UnableToMoveExeption("Unable to move completed action!");

        //If the JSON is empty
        if(actionPositionDTO.getPrevId() == null && actionPositionDTO.getNextId() == null) throw new NoSuchElementException("Both previous and next ids are empty!");

        //If user wants to move action to the beginning
        if(actionPositionDTO.getPrevId() == null) {

            if(!actionRepo.existsById(actionPositionDTO.getNextId())) throw new NoSuchElementException("No such action with id: " + actionPositionDTO.getNextId());

            if(actionRepo.existsByActionIdAndStatus(actionPositionDTO.getNextId(), Status.COMPLETED)) {
                throw new UnableToMoveExeption("Unable to move the beginning, next action is already completed!");
            }
        }
        //If user wants to move action to the end
        else if(actionPositionDTO.getNextId() == null) {
            if(!actionRepo.existsById(actionPositionDTO.getPrevId())) throw new NoSuchElementException("No such action with id: " + actionPositionDTO.getPrevId());
        }
        //If user wants to move action to the middle
        else {
            if(!actionRepo.existsById(actionPositionDTO.getNextId()) || !actionRepo.existsById(actionPositionDTO.getPrevId())) throw new NoSuchElementException("No such action with id: " + actionPositionDTO.getNextId() + " or " + actionPositionDTO.getPrevId());

            //If the status of nextId is Completed
            if(actionRepo.existsByActionIdAndStatus(actionPositionDTO.getNextId(), Status.COMPLETED)) {
                throw new UnableToMoveExeption("Unable to move because it is in the block of completed tasks!");
            }

        }
    }

    //Our Action has the Now_Working status
    private void setNowWorkingToNextAction(Action action) {

        Goal goal = action.getGoal();
        int index = goal.getActions().indexOf(action);

        if(index == -1) throw new NoSuchElementException("No such action in the goal!");

        Action nextAction = goal.getActions().get(index + 1);

        action.setStatus(Status.NOT_COMPLETED);
        nextAction.setStatus(Status.NOW_WORKING);
        goal.setNowWorkingAction(nextAction);

        goalRepo.save(goal);

    }

    //Changes the now working action for goal
    private void setNowWorkingToMoving(Action moving, Action next) {

        Goal goal = moving.getGoal();
        moving.setStatus(Status.NOW_WORKING);
        next.setStatus(Status.NOT_COMPLETED);
        goal.setNowWorkingAction(moving);

        goalRepo.save(goal);
    }


    @Transactional
    public void delete(Long actionId) {
        // загрузим action и goal в одном графе (чтобы они были managed)
        Action action = actionRepo.findWithGoalAndRoadmap(actionId)
                .orElseThrow(() -> new NoSuchElementException("No such action with that id!"));

        if(action.getStatus() == Status.COMPLETED)
            throw new ValidationException("Cant delete completed action!");

        Goal goal = goalRepo.findGoalWithActionsByActionIdGraph(actionId)
                .orElseThrow(() -> new NoSuchElementException("No such goal for action!"));

        // --- ваша логика смены статусов ---
        if(action.getStatus() == Status.NOW_WORKING) {
            // если это "последнее" действие в цели — обновляем goal/roadmap как у вас
            if(goal.findLastAction().equals(action)) {
                goal.setStatus(Status.COMPLETED);
                Roadmap roadmap = roadmapRepo.findRoadmapWithGoalsById(goal.getRoadmap().getRoadmapId()).get();
                int id = roadmap.getGoals().indexOf(goal);
                if(id == roadmap.getGoals().size() - 1) {
                    roadmap.setStatus(Status.COMPLETED);
                } else {
                    Goal nextGoal = goalRepo.findGoalWithActionsByGoalId(roadmap.getGoals().get(id + 1).getGoalId()).get();
                    nextGoal.setStatus(Status.NOW_WORKING);
                    if(nextGoal.getNowWorkingAction() == null) {
                        nextGoal.setNowWorkingAction(nextGoal.findFirstNotCompletedAction());
                    }
                    roadmap.setNowWorkingGoal(nextGoal);
                    roadmapRepo.save(roadmap); // сохраняем roadmap и связанные изменения
                }
            } else {
                // если не последний — переключаем на следующее действие и сохраняем goal
                int id = goal.getActions().indexOf(action);
                Action nextAction = goal.getActions().get(id + 1);
                nextAction.setStatus(Status.NOW_WORKING);
                goal.setNowWorkingAction(nextAction);
                // сохранение goal ниже (после отвязки action)
            }
        }

        // --- ВАЖНО: отвязываем action от goal и удаляем через коллекцию родителя ---
        // Если goal.nowWorkingAction указывает на action — обнуляем
        if (goal.getNowWorkingAction() != null && goal.getNowWorkingAction().equals(action)) {
            goal.setNowWorkingAction(null);
        }

        // Удаляем action из коллекции goals (Goal.removeAction у вас уже ставит action.setGoal(null))
        goal.removeAction(action); // ваша реализация removeAction делает action.setGoal(null)
        // Сохраняем goal — orphanRemoval = true удалит action из БД
        goalRepo.save(goal);
        // commit транзакции -> JPA выполнит необходимые SQL: обновление goals.working_id (NULL) и DELETE action
    }

}
