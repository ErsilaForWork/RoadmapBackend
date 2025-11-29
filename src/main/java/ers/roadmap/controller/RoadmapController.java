package ers.roadmap.controller;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.DTO.model.input.RoadmapInput;
import ers.roadmap.DTO.model.output.RoadmapDTO;
import ers.roadmap.DTO.patch.PatchRoadmapDTO;
import ers.roadmap.exceptions.ConstraintsNotMetException;
import ers.roadmap.model.CustomMessage;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.security.model.AppUser;
import ers.roadmap.security.service.AppUserService;
import ers.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final AppUserService userService;
    private final RoadmapMapper roadmapMapper;

    public RoadmapController(RoadmapService roadmapService, AppUserService userService, RoadmapMapper roadmapMapper) {
        this.roadmapService = roadmapService;
        this.userService = userService;
        this.roadmapMapper = roadmapMapper;
    }


    @PostMapping("/roadmap")
    public ResponseEntity<?> createRoadmap(@Valid @RequestBody RoadmapInput roadmapDTO, BindingResult br, @AuthenticationPrincipal UserDetails userDetails) {


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

        return new ResponseEntity<>(roadmapService.createProduct(roadmapDTO, owner), HttpStatus.OK);
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

    @GetMapping("/roadmap/{id}")
    public ResponseEntity<?> getRoadmapById(@PathVariable("id") Long roadmapId) {
        try{
            RoadmapDTO roadmapDTO = roadmapService.getById(roadmapId);
            return ResponseEntity.ok(roadmapDTO);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(new CustomMessage("No such roadmap id!"),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @roadmapService.isOwner(authentication.name, #id)")
    @DeleteMapping("/roadmap/{id}")
    public ResponseEntity<?> deleteRoadmap(@PathVariable("id") Long id) {
        roadmapService.deleteById(id);
        return new ResponseEntity<>(new CustomMessage("Succesfully deleted"), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @roadmapService.isOwner(authentication.name, #roadmapId)")
    @PatchMapping("/roadmap/{id}")
    public ResponseEntity<?> updateRoadmap(@PathVariable("id") Long roadmapId,  @RequestBody @Valid PatchRoadmapDTO patchDTO, BindingResult br) {

        if(br.hasErrors()) {
            return new ResponseEntity<>(new CustomMessage("Bad title"), HttpStatus.BAD_REQUEST);
        }

        RoadmapDTO roadmapDTO;
        try{
            roadmapDTO = roadmapService.partialUpdate(roadmapId, patchDTO);
        }catch (ConstraintsNotMetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(roadmapDTO, HttpStatus.OK);
    }

}
