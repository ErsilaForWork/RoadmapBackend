package ers.roadmap.DTO.model.output;

import ers.roadmap.security.model.AppRole;

public record AppUserDTOForRoadmap (Long userId, String username, AppRole role) {}
