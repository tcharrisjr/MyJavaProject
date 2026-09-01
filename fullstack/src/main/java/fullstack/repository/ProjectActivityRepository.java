package fullstack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fullstack.model.ProjectActivity;

@Repository
public interface ProjectActivityRepository
        extends JpaRepository<ProjectActivity, Long> {

    // =========================================================
    // PROJECT ACTIVITY
    //
    // Returns all activity for a project,
    // newest activity first.
    // =========================================================

    List<ProjectActivity>
        findByProject_IdOrderByCreatedAtDesc(
            Long projectId
        );

    // =========================================================
    // TASK ACTIVITY
    //
    // Returns all activity for a task,
    // newest activity first.
    // =========================================================

    List<ProjectActivity>
        findByTask_IdOrderByCreatedAtDesc(
            Long taskId
        );

    // =========================================================
    // PROJECT ACTIVITY COUNT
    // =========================================================

    long countByProject_Id(
            Long projectId
    );
}