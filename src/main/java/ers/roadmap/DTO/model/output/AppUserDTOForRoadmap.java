package ers.roadmap.DTO.model.output;

import ers.roadmap.DTO.AppRoleDTO;

public record AppUserDTOForRoadmap (Long userId, String username, AppRoleDTO role) {}
