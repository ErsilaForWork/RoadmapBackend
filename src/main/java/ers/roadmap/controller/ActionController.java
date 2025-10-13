package ers.roadmap.controller;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.model.Action;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.service.ActionService;
import ers.roadmap.service.GoalService;
import ers.roadmap.service.RoadmapService;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/action")
public class ActionController {

    private final GoalService goalService;
    private final ActionService actionService;
    private final RoadmapService roadmapService;
    private final RoadmapMapper roadmapMapper;

    public ActionController(GoalService goalService, ActionService actionService, RoadmapService roadmapService, RoadmapMapper roadmapMapper) {
        this.goalService = goalService;
        this.actionService = actionService;
        this.roadmapService = roadmapService;
        this.roadmapMapper = roadmapMapper;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN') or @roadmapService.isOwner(authentication.name, #actionId)")
    @PutMapping("/complete/{id}")
    public ResponseEntity<?> complete(@PathVariable("id") Long actionId) {

        System.out.println("Action complete start ---------------------------------------------");
        Action action;

        try {
            //Returns an action with the goal and roadmap loaded
            action = actionService.validateToComplete(actionId);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(new CustomMessage("No such action id"), HttpStatus.BAD_REQUEST);
        }catch (ValidationException e) {
            //Action id is not equal to NowWorking ID of the goal entity
            return new ResponseEntity<>(new CustomMessage("Action id cant be completed"), HttpStatus.BAD_REQUEST);
        }

        Goal goal = goalService.findGoalWithActionsByActionId(action.getActionId());

        Roadmap roadmap = goal.getRoadmap();
        List<Goal> goals = goalService.findGoalsWithActionsByRoadmapId(roadmap.getRoadmapId());
        roadmap.setGoals(goals);

        //Set Completed for goal if it is last action, and for action always
        actionService.setActionCompleted(action, goal, roadmap);
        goalService.setCalculatedPercent(goal, roadmap);

        System.out.println("Action complete end ---------------------------------------------");
        roadmapService.save(roadmap);
        return new ResponseEntity<>(roadmapMapper.toDTO(roadmap), HttpStatus.OK);
    }

}
