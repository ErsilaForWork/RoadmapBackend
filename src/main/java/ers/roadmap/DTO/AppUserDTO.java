package ers.roadmap.DTO;

import ers.roadmap.DTO.model.output.RoadmapPairDTO;
import ers.roadmap.model.Pair;
import ers.roadmap.security.model.AppRole;

import java.time.LocalDateTime;
import java.util.List;

public record AppUserDTO(Long userId, String username, String email, String phoneNumber, LocalDateTime creationTime, AppRole role, List<RoadmapPairDTO> roadmaps) {
}
