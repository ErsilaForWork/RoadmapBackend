package ers.roadmap.controller;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.DTO.patch.PatchActionDTO;
import ers.roadmap.DTO.patch.PatchPositionDTO;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.exceptions.UnableToMoveExeption;
import ers.roadmap.model.Action;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.security.service.AppUserService;
import ers.roadmap.service.ActionService;
import ers.roadmap.service.GoalService;
import ers.roadmap.service.RoadmapService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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
    private final AppUserService userService;

    public ActionController(GoalService goalService, ActionService actionService, RoadmapService roadmapService, RoadmapMapper roadmapMapper, AppUserService userService) {
        this.goalService = goalService;
        this.actionService = actionService;
        this.roadmapService = roadmapService;
        this.roadmapMapper = roadmapMapper;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @actionService.isOwner(authentication.name, #actionId)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAction(@PathVariable("id") Long actionId) {

        try {
            actionService.delete(actionId);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new CustomMessage(e.getMessage()));
        }

        return ResponseEntity.ok(new CustomMessage("Successfully deleted!"));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @actionService.isOwner(authentication.name, #actionId)")
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(@Valid @RequestBody PatchActionDTO actionDTO, BindingResult br, @PathVariable("id") Long actionId) {

        if(br.hasErrors()) {
            return new ResponseEntity<>(new CustomMessage("Bad request body!"), HttpStatus.BAD_REQUEST);
        }

        try{
            actionService.partialUpdate(actionId, actionDTO);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()),HttpStatus.BAD_REQUEST);
        }catch (ConstraintsNotMetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @actionService.isOwner(authentication.name, #actionId)")
    @PatchMapping("/move/{id}")
    public ResponseEntity<?> moveActionPlace(@RequestBody PatchPositionDTO actionPositionDTO, @PathVariable("id") Long actionId) {

        try {
            actionService.validateToMove(actionId, actionPositionDTO);
        }catch (UnableToMoveExeption | NoSuchElementException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        actionService.move(actionId, actionPositionDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @actionService.isOwner(authentication.name, #actionId)")
    @PutMapping("/complete/{id}")
    @Transactional
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
        userService.incrementStreak(roadmap.getOwner());

        return new ResponseEntity<>(roadmapMapper.toDTO(roadmap), HttpStatus.OK);
    }

}
