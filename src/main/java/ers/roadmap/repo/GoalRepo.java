package ers.roadmap.repo;

import ers.roadmap.model.Goal;
import ers.roadmap.model.enums.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepo extends JpaRepository<Goal, Long> {

    @Query("select g from Goal g where g.goalId = (select a.goal.goalId from Action a where a.actionId = :actionId)")
    @EntityGraph("goal_with_actions")
    Optional<Goal> findGoalWithActionsByActionIdGraph(@Param("actionId") Long actionId);

    @Query("select g from Goal g where g.roadmap.roadmapId = :roadmapId order by g.position ASC")
    @EntityGraph("goal_with_actions")
    List<Goal> findGoalsWithActionsByRoadmapIdGraph(@Param("roadmapId") Long roadmapId);

    @Query("SELECT g FROM Goal g where g.roadmap.roadmapId = :roadmapId ORDER BY g.position ASC")
    @EntityGraph("goal_with_actions")
    List<Goal> findGoalsWithActionsUsingRoadmapId(@Param("roadmapId") Long roadmapId);

    boolean existsByGoalIdAndRoadmap_Owner_Username(Long goalId, String roadmapOwnerUsername);

    boolean existsByGoalIdAndStatus(Long goalId, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Goal g WHERE g.goalId = :id")
    @EntityGraph("goal_with_actions")
    Optional<Goal> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query(value = """
        WITH ordered AS (
          SELECT goal_id, row_number() OVER (ORDER BY position NULLS LAST, goal_id) AS rn
          FROM goals
          WHERE roadmap_id = :roadmapId
        )
        UPDATE goals g
        SET position = ordered.rn * :step
        FROM ordered
        WHERE g.goal_id = ordered.goal_id
        """, nativeQuery = true)
    void reindexGoalsByRoadmap(@Param("roadmapId") Long roadmapId, @Param("step") Long step);

}
