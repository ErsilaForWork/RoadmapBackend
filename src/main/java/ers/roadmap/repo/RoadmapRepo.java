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

    @Query("""
        select distinct r
        from Roadmap r
        left join fetch r.goals g
        where r.roadmapId = (
            select g2.roadmap.roadmapId
            from Goal g2
            where g2.goalId = :goalId
        )
        """)
    Optional<Roadmap> findRoadmapWithGoalsByGoalId(@Param("goalId") Long goalId);

    @Query("select r from Roadmap r where r.owner.username = :username")
    List<Roadmap> findAllByOwnerUsername(@Param("username") String username);

    @Query("select r from Roadmap r")
    @EntityGraph("roadmap_with_goals")
    List<Roadmap> getAllEfficient();

    List<Roadmap> getRoadmapsByOwner_Username(String ownerUsername);

    List<Roadmap> getRoadmapsByOwner_UsernameAndStatus(String ownerUsername, Status status);
}
