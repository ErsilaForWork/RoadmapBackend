package ers.roadmap.security.repo;

import ers.roadmap.security.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findAppUserByUsername(String username);

    @Query("select u from AppUser u where u.username = :username")
    @EntityGraph("user_with_roadmaps")
    Optional<AppUser> findAppUserByUsernameWithRoadmaps(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM AppUser u")
    @EntityGraph(attributePaths = {"roadmaps", "role"})
    List<AppUser> getAllWithRoadmap();

    Optional<AppUser> findByEmail(String email);
}
