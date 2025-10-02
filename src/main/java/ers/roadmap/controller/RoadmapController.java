package ers.roadmap.controller;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.DTO.model.input.RoadmapInput;
import ers.roadmap.DTO.model.output.RoadmapDTO;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.service.AppUserService;
import ers.roadmap.service.GoalService;
import ers.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final AppUserService userService;
    private final GoalService goalService;

    public RoadmapController(RoadmapService roadmapService, AppUserService userService, RoadmapMapper roadmapMapper, GoalService goalService) {
        this.roadmapService = roadmapService;
        this.userService = userService;
        this.goalService = goalService;
    }


    @PostMapping("/roadmap")
    public ResponseEntity<?> createRoadmap(@Valid @RequestBody RoadmapInput roadmapDTO, BindingResult br, @AuthenticationPrincipal UserDetails userDetails ) {


        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new CustomMessage("Unauthorized"));
        }

        if(br.hasErrors()) {
            return ResponseEntity.badRequest().body(new CustomMessage("Invalid input data"));
        }

        AppUser owner;

        try {
            owner = userService.findByUsername(userDetails.getUsername());
        }catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(new CustomMessage("No such user!"), HttpStatus.BAD_REQUEST);
        }

        Roadmap roadmap = roadmapService.mapWithOwner(roadmapDTO, owner);
        return new ResponseEntity<>(roadmapService.save(roadmap), HttpStatus.OK);

    }

    @GetMapping("/user/roadmaps")
    public List<RoadmapDTO> getAllRoadmapsOfUser(@AuthenticationPrincipal UserDetails userDetails) {
        return roadmapService.getAllEficientWithUsername(userDetails.getUsername());
    }

    @GetMapping("/user/roadmaps/completed")
    public List<RoadmapDTO> getAllCompletedRoadmapsOfUser(@AuthenticationPrincipal UserDetails userDetails) {
        return roadmapService.getAllWithUsernameAndStatus(userDetails.getUsername(), Status.COMPLETED);
    }

    @GetMapping("/user/roadmaps/notcompleted")
    public List<RoadmapDTO> getAllNotCompletedRoadmapsOfUser(@AuthenticationPrincipal UserDetails userDetails) {
        return roadmapService.getAllWithUsernameAndStatus(userDetails.getUsername(), Status.NOT_COMPLETED);
    }

    @GetMapping("/roadmaps")
    public List<RoadmapDTO> getAll() {
        return roadmapService.getAllEficient();
    }

}
