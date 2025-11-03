package ers.roadmap.repo;

import ers.roadmap.model.Action;
import ers.roadmap.model.Goal;
import ers.roadmap.model.enums.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActionRepo extends JpaRepository<Action, Long> {

    // 1) action + goal + roadmap
    @Query("select a from Action a join fetch a.goal g join fetch g.roadmap r where a.actionId = :id")
    Optional<Action> findWithGoalAndRoadmap(@Param("id") Long id);

    boolean existsByActionIdAndStatus(Long actionId, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Action a WHERE a.actionId = :id")
    Optional<Action> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query(value = """
        WITH ordered AS (
          SELECT action_id, row_number() OVER (ORDER BY position) AS rn
          FROM actions
          WHERE goal_id = :goalId
        )
        UPDATE actions a
        SET position = ordered.rn * :step
        FROM ordered
        WHERE a.action_id = ordered.action_id
        """, nativeQuery = true)
    void reindexActionsByGoal(@Param("goalId") Long goalId, @Param("step") Long step);

}
