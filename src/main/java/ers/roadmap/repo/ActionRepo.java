package ers.roadmap.repo;

import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActionRepo extends JpaRepository<Action, Long> {

    // 1) action + goal + roadmap
    @Query("select a from Action a join fetch a.goal g join fetch g.roadmap r where a.actionId = :id")
    Optional<Action> findWithGoalAndRoadmap(@Param("id") Long id);

}
