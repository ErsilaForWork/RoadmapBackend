package ers.roadmap.controller;

import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.service.GoalService;
import ers.roadmap.service.RoadmapService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/goal")
public class GoalController {

    private final GoalService goalService;
    private final RoadmapService roadmapService;
    private final RoadmapMapper roadmapMapper;

    public GoalController(GoalService goalService, RoadmapService roadmapService, RoadmapMapper roadmapMapper) {
        this.goalService = goalService;
        this.roadmapService = roadmapService;
        this.roadmapMapper = roadmapMapper;
    }

}
