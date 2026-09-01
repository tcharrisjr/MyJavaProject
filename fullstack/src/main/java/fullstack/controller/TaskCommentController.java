package fullstack.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.comment.TaskCommentRequest;
import fullstack.dto.comment.TaskCommentResponse;
import fullstack.service.TaskCommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TaskCommentController {

    private final TaskCommentService
            taskCommentService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskCommentController(
            TaskCommentService taskCommentService) {

        this.taskCommentService =
                taskCommentService;
    }

    // =========================================================
    // GET TASK COMMENTS
    // =========================================================
    //
    // GET
    //
    // /api/projects/{projectId}/tasks/{taskId}/comments
    //
    // Returns all comments for the selected task.
    //
    // Comments are returned oldest -> newest.
    // =========================================================

    @GetMapping(
            "/projects/{projectId}/tasks/{taskId}/comments"
    )
    public ResponseEntity<List<TaskCommentResponse>>
            getComments(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        List<TaskCommentResponse> comments =
                taskCommentService
                        .getComments(
                                projectId,
                                taskId,
                                email
                        );

        return ResponseEntity.ok(
                comments
        );
    }

    // =========================================================
    // CREATE TASK COMMENT
    // =========================================================
    //
    // POST
    //
    // /api/projects/{projectId}/tasks/{taskId}/comments
    //
    // Example request:
    //
    // {
    //     "commentText": "Backend work is complete."
    // }
    //
    // The authenticated user automatically becomes
    // the comment author.
    // =========================================================

    @PostMapping(
            "/projects/{projectId}/tasks/{taskId}/comments"
    )
    public ResponseEntity<TaskCommentResponse>
            createComment(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    @Valid
                    @RequestBody
                    TaskCommentRequest request,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        TaskCommentResponse createdComment =
                taskCommentService
                        .createComment(
                                projectId,
                                taskId,
                                request,
                                email
                        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        createdComment
                );
    }

    // =========================================================
    // UPDATE TASK COMMENT
    // =========================================================
    //
    // PUT
    //
    // /api/projects/{projectId}/tasks/{taskId}/comments/{commentId}
    //
    // Only the original comment author can edit it.
    //
    // Example request:
    //
    // {
    //     "commentText": "Updated comment text."
    // }
    // =========================================================

    @PutMapping(
            "/projects/{projectId}/tasks/{taskId}/comments/{commentId}"
    )
    public ResponseEntity<TaskCommentResponse>
            updateComment(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    @PathVariable
                    Long commentId,

                    @Valid
                    @RequestBody
                    TaskCommentRequest request,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        TaskCommentResponse updatedComment =
                taskCommentService
                        .updateComment(
                                projectId,
                                taskId,
                                commentId,
                                request,
                                email
                        );

        return ResponseEntity.ok(
                updatedComment
        );
    }

    // =========================================================
    // DELETE TASK COMMENT
    // =========================================================
    //
    // DELETE
    //
    // /api/projects/{projectId}/tasks/{taskId}/comments/{commentId}
    //
    // Only the original author can delete the comment.
    // =========================================================

    @DeleteMapping(
            "/projects/{projectId}/tasks/{taskId}/comments/{commentId}"
    )
    public ResponseEntity<Void>
            deleteComment(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    @PathVariable
                    Long commentId,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        taskCommentService
                .deleteComment(
                        projectId,
                        taskId,
                        commentId,
                        email
                );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =========================================================
    // COMMENT COUNT
    // =========================================================
    //
    // GET
    //
    // /api/projects/{projectId}/tasks/{taskId}/comments/count
    //
    // This endpoint will be useful later for:
    //
    // - comment badges
    // - task cards
    // - dashboard reporting
    //
    // Example response:
    //
    // 4
    // =========================================================

    @GetMapping(
            "/projects/{projectId}/tasks/{taskId}/comments/count"
    )
    public ResponseEntity<Long>
            getCommentCount(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        long count =
                taskCommentService
                        .getCommentCount(
                                projectId,
                                taskId,
                                email
                        );

        return ResponseEntity.ok(
                count
        );
    }

    // =========================================================
    // AUTHENTICATED EMAIL HELPER
    // =========================================================
    //
    // Spring Security stores the authenticated login identity
    // in Principal.getName().
    //
    // In this application that identity is the user's email.
    // =========================================================

    private String getAuthenticatedEmail(
            Principal principal) {

        if (principal == null
                || principal.getName() == null
                || principal.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
            );
        }

        return principal
                .getName()
                .trim();
    }
}