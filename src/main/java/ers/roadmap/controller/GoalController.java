package ers.roadmap.controller;

import ers.roadmap.DTO.patch.PatchGoalDTO;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/goal")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
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
