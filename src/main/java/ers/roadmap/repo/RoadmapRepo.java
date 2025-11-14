package ers.roadmap.repo;

import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapRepo extends JpaRepository<Roadmap, Long> {

    @Query("select r from Roadmap r where r.owner.username = :username")
    List<Roadmap> findAllByOwnerUsername(@Param("username") String username);

    List<Roadmap> getRoadmapsByOwner_Username(String ownerUsername);

    List<Roadmap> getRoadmapsByOwner_UsernameAndStatus(String ownerUsername, Status status);

    @Query("select r from Roadmap r where r.roadmapId = :id")
    @EntityGraph("roadmap_with_owner")
    Optional<Roadmap> findRoadmapById(@Param("id") Long roadmapId);

    @Query("select r from Roadmap r where r.roadmapId = :id")
    @EntityGraph("roadmap_with_goals")
    Optional<Roadmap> findRoadmapWithGoalsById(@Param("id") Long roadmapId);

    Roadmap getRoadmapByRoadmapId(Long roadmapId);
}
