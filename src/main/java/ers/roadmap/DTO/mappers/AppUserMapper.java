package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.AppUserDTO;
import ers.roadmap.model.Pair;
import ers.roadmap.model.Roadmap;
import ers.roadmap.security.model.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    private final PairMapper pairMapper;
    private final AppRoleMapper appRoleMapper;

    public AppUserMapper(PairMapper pairMapper, AppRoleMapper appRoleMapper) {
        this.pairMapper = pairMapper;
        this.appRoleMapper = appRoleMapper;
    }

    public AppUserDTO toDto(AppUser user) {
        return new AppUserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreationTime(),
                appRoleMapper.toDTO(user.getRole()),
                user.getRoadmaps().stream()
                        .map(r -> (Pair<Long, String>) new Pair(r.getRoadmapId(), r.getTitle()))
                        .map(pairMapper::toDTO)
                        .toList(),
                user.getStreak(),
                user.isStreakBroken()
        );
    }

}
