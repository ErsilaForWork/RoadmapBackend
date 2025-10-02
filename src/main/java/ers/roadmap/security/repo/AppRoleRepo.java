package ers.roadmap.security.repo;

import ers.roadmap.security.model.AppRole;
import ers.roadmap.security.model.EnumAppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRoleRepo extends JpaRepository<AppRole, Long> {
    List<AppRole> getByRole(EnumAppRole role);

    AppRole getAppRoleByRole(EnumAppRole role);

    boolean existsAppRoleByRole(EnumAppRole role);
}
