package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.AppRoleDTO;
import ers.roadmap.security.model.AppRole;
import org.springframework.stereotype.Component;

@Component
public class AppRoleMapper {

    public AppRoleDTO toDTO(AppRole appRole) {
        return new AppRoleDTO(appRole.toString());
    }

}
