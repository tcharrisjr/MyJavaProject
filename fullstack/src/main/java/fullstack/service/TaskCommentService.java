package fullstack.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.comment.TaskCommentRequest;
import fullstack.dto.comment.TaskCommentResponse;
import fullstack.model.ActivityType;
import fullstack.model.AppUser;
import fullstack.model.Project;
import fullstack.model.Task;
import fullstack.model.TaskComment;
import fullstack.repository.TaskCommentRepository;
import fullstack.repository.TaskRepository;
import fullstack.repository.UserRepository;

@Service
@Transactional
public class TaskCommentService {

    private final TaskCommentRepository
            taskCommentRepository;

    private final TaskRepository
            taskRepository;

    private final UserRepository
            userRepository;

    /*
     * Sequence 15A
     *
     * Central activity-history service.
     */
    private final ProjectActivityService
            projectActivityService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskCommentService(
            TaskCommentRepository taskCommentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectActivityService projectActivityService) {

        this.taskCommentRepository =
                taskCommentRepository;

        this.taskRepository =
                taskRepository;

        this.userRepository =
                userRepository;

        this.projectActivityService =
                projectActivityService;
    }

    // =========================================================
    // GET COMMENTS FOR TASK
    // =========================================================

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getComments(
            Long projectId,
            Long taskId,
            String email) {

        /*
         * Verify project/task ownership before retrieving
         * comment information.
         */
        getOwnedTask(
                projectId,
                taskId,
                email
        );

        return taskCommentRepository
                .findByTaskIdOrderByCreatedAtAsc(
                        taskId
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    // =========================================================
    // CREATE COMMENT
    // =========================================================

    public TaskCommentResponse createComment(
            Long projectId,
            Long taskId,
            TaskCommentRequest request,
            String email) {

        Task task =
                getOwnedTask(
                        projectId,
                        taskId,
                        email
                );

        AppUser author =
                getAuthenticatedUser(
                        email
                );

        String commentText =
                normalizeCommentText(
                        request.getCommentText()
                );

        TaskComment comment =
                new TaskComment();

        comment.setTask(
                task
        );

        comment.setUser(
                author
        );

        comment.setCommentText(
                commentText
        );

        /*
         * Timestamp is controlled by the service.
         */
        comment.setCreatedAt(
                LocalDateTime.now()
        );

        TaskComment savedComment =
                taskCommentRepository.save(
                        comment
                );

        /*
         * =====================================================
         * SEQUENCE 15A - COMMENT ADDED
         * =====================================================
         *
         * Store the comment text as the new value so the
         * activity timeline has useful context.
         */

        Project project =
                task.getProject();

        projectActivityService
                .recordTaskFieldChange(
                        project,
                        task,
                        email,
                        ActivityType.COMMENT_ADDED,
                        "comment",
                        null,
                        savedComment.getCommentText(),
                        "Added a comment to task \""
                                + task.getTitle()
                                + "\""
                );

        return toResponse(
                savedComment
        );
    }

    // =========================================================
    // UPDATE COMMENT
    // =========================================================

    public TaskCommentResponse updateComment(
            Long projectId,
            Long taskId,
            Long commentId,
            TaskCommentRequest request,
            String email) {

        /*
         * Keep the owned Task because Sequence 15A needs its
         * Project and Task relationships for the activity row.
         */
        Task task =
                getOwnedTask(
                        projectId,
                        taskId,
                        email
                );

        TaskComment comment =
                getCommentForTask(
                        taskId,
                        commentId
                );

        verifyCommentAuthor(
                comment,
                email
        );

        /*
         * Capture the previous comment text before modifying
         * the entity.
         */
        String oldCommentText =
                comment.getCommentText();

        String newCommentText =
                normalizeCommentText(
                        request.getCommentText()
                );

        comment.setCommentText(
                newCommentText
        );

        comment.setUpdatedAt(
                LocalDateTime.now()
        );

        TaskComment savedComment =
                taskCommentRepository.save(
                        comment
                );

        /*
         * =====================================================
         * SEQUENCE 15A - COMMENT UPDATED
         * =====================================================
         *
         * Only generate an audit record when the normalized
         * comment text actually changed.
         */

        if (!Objects.equals(
                oldCommentText,
                newCommentText)) {

            projectActivityService
                    .recordTaskFieldChange(
                            task.getProject(),
                            task,
                            email,
                            ActivityType.COMMENT_UPDATED,
                            "comment",
                            oldCommentText,
                            newCommentText,
                            "Updated a comment on task \""
                                    + task.getTitle()
                                    + "\""
                    );
        }

        return toResponse(
                savedComment
        );
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================

    public void deleteComment(
            Long projectId,
            Long taskId,
            Long commentId,
            String email) {

        Task task =
                getOwnedTask(
                        projectId,
                        taskId,
                        email
                );

        TaskComment comment =
                getCommentForTask(
                        taskId,
                        commentId
                );

        verifyCommentAuthor(
                comment,
                email
        );

        /*
         * Capture the text before deletion.
         */
        String deletedCommentText =
                comment.getCommentText();

        /*
         * =====================================================
         * SEQUENCE 15A - COMMENT DELETED
         * =====================================================
         *
         * Record the audit event BEFORE deleting the comment.
         *
         * The activity itself points to the Task, not to the
         * TaskComment row, so deleting the comment does not
         * remove the audit event.
         */

        projectActivityService
                .recordTaskFieldChange(
                        task.getProject(),
                        task,
                        email,
                        ActivityType.COMMENT_DELETED,
                        "comment",
                        deletedCommentText,
                        null,
                        "Deleted a comment from task \""
                                + task.getTitle()
                                + "\""
                );

        taskCommentRepository.delete(
                comment
        );
    }

    // =========================================================
    // COMMENT COUNT
    // =========================================================

    @Transactional(readOnly = true)
    public long getCommentCount(
            Long projectId,
            Long taskId,
            String email) {

        getOwnedTask(
                projectId,
                taskId,
                email
        );

        return taskCommentRepository
                .countByTaskId(
                        taskId
                );
    }

    // =========================================================
    // OWNED TASK HELPER
    // =========================================================

    private Task getOwnedTask(
            Long projectId,
            Long taskId,
            String email) {

        return taskRepository
                .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                        taskId,
                        projectId,
                        email
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Task not found."
                                )
                );
    }

    // =========================================================
    // AUTHENTICATED USER HELPER
    // =========================================================

    private AppUser getAuthenticatedUser(
            String email) {

        if (email == null
                || email.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
            );
        }

        return userRepository
                .findByEmailIgnoreCase(
                        email.trim()
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user not found."
                                )
                );
    }

    // =========================================================
    // COMMENT FOR TASK HELPER
    // =========================================================

    private TaskComment getCommentForTask(
            Long taskId,
            Long commentId) {

        TaskComment comment =
                taskCommentRepository
                        .findById(
                                commentId
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Comment not found."
                                        )
                        );

        if (comment.getTask() == null
                || comment.getTask().getId() == null
                || !comment.getTask()
                        .getId()
                        .equals(taskId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found."
            );
        }

        return comment;
    }

    // =========================================================
    // COMMENT AUTHOR PROTECTION
    // =========================================================

    private void verifyCommentAuthor(
            TaskComment comment,
            String email) {

        if (comment.getUser() == null
                || comment.getUser().getEmail() == null
                || email == null
                || !comment.getUser()
                        .getEmail()
                        .equalsIgnoreCase(
                                email
                        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You may only modify your own comments."
            );
        }
    }

    // =========================================================
    // COMMENT TEXT NORMALIZATION
    // =========================================================

    private String normalizeCommentText(
            String commentText) {

        if (commentText == null
                || commentText.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comment text is required."
            );
        }

        String normalized =
                commentText.trim();

        if (normalized.length() > 2000) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comment text cannot exceed 2000 characters."
            );
        }

        return normalized;
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private TaskCommentResponse toResponse(
            TaskComment comment) {

        TaskCommentResponse response =
                new TaskCommentResponse();

        response.setId(
                comment.getId()
        );

        if (comment.getTask() != null) {

            response.setTaskId(
                    comment.getTask()
                            .getId()
            );
        }

        if (comment.getUser() != null) {

            response.setUserId(
                    comment.getUser()
                            .getId()
            );

            response.setAuthorName(
                    comment.getUser()
                            .getName()
            );

            response.setAuthorEmail(
                    comment.getUser()
                            .getEmail()
            );
        }

        response.setCommentText(
                comment.getCommentText()
        );

        response.setCreatedAt(
                comment.getCreatedAt()
        );

        response.setUpdatedAt(
                comment.getUpdatedAt()
        );

        return response;
    }
}