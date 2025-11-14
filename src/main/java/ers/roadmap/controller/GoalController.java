package ers.roadmap.controller;

import ers.roadmap.DTO.patch.PatchPositionDTO;
import ers.roadmap.DTO.patch.PatchGoalDTO;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.exceptions.UnableToMoveExeption;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@RequestMapping("/goal")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @goalService.isOwner(authentication.name, #goalId)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable("id") Long goalId) {
        try {
            goalService.delete(goalId);
        }catch (Exception e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()) , HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(new CustomMessage("Successfully deleted!"));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @goalService.isOwner(authentication.name, #goalId)")
    @PatchMapping("/move/{id}")
    public ResponseEntity<?> moveGoalPlace(@RequestBody PatchPositionDTO goalPositionDTO, @PathVariable("id") Long goalId) {

        try {
            goalService.validateToMove(goalId, goalPositionDTO);
        }catch (UnableToMoveExeption | NoSuchElementException e) {
            return new ResponseEntity<>(new CustomMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        goalService.move(goalId, goalPositionDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @goalService.isOwner(authentication.name, #goalId)")
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(@Valid @RequestBody PatchGoalDTO goalDTO, BindingResult br, @PathVariable("id") Long goalId) {

        if(br.hasErrors()) {
            return new ResponseEntity<>(new CustomMessage("Bad request body"), HttpStatus.BAD_REQUEST);
        }

        try{
            goalService.partialUpdate(goalId, goalDTO);
        }catch(ConstraintsNotMetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
