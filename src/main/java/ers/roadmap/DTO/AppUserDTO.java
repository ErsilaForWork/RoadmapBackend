package ers.roadmap.DTO;

import ers.roadmap.DTO.model.output.RoadmapPairDTO;
import ers.roadmap.security.model.AppRole;

import java.time.LocalDateTime;
import java.util.List;

public record AppUserDTO(Long userId, String username, String email, LocalDateTime creationTime, AppRoleDTO role, List<RoadmapPairDTO> roadmaps, Integer streak, Boolean streakBroken) {
}
