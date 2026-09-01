package fullstack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fullstack.model.TaskComment;

public interface TaskCommentRepository
        extends JpaRepository<TaskComment, Long> {

    /*
     * Return all comments for a task in chronological order.
     *
     * Oldest comments appear first so the UI can render the
     * discussion naturally from top to bottom.
     */
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    /*
     * Useful for cleanup checks, tests, reporting, and later
     * dashboard/comment-count features.
     */
    long countByTaskId(Long taskId);
}