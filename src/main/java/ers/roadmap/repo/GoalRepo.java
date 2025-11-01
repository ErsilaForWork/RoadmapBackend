package ers.roadmap.repo;

import ers.roadmap.model.Goal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepo extends JpaRepository<Goal, Long> {

    @Query("select g from Goal g where g.goalId = (select a.goal.goalId from Action a where a.actionId = :actionId)")
    @EntityGraph("goal_with_actions")
    Optional<Goal> findGoalWithActionsByActionIdGraph(@Param("actionId") Long actionId);

    @Query("select g from Goal g where g.roadmap.roadmapId = :roadmapId order by g.goalId")
    @EntityGraph("goal_with_actions")
    List<Goal> findGoalsWithActionsByRoadmapIdGraph(@Param("roadmapId") Long roadmapId);

    @Query("SELECT g FROM Goal g where g.roadmap.roadmapId = :roadmapId ORDER BY g.goalId")
    @EntityGraph("goal_with_actions")
    List<Goal> findGoalsWithActionsUsingRoadmapId(@Param("roadmapId") Long roadmapId);

    boolean existsByGoalIdAndRoadmap_Owner_Username(Long goalId, String roadmapOwnerUsername);
}
