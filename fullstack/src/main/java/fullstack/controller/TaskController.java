package fullstack.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.task.TaskRequest;
import fullstack.dto.task.TaskResponse;

import fullstack.service.ProjectService;
import fullstack.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService
            taskService;

    private final ProjectService
            projectService;

    // =========================================================
    // ALLOWED SORT FIELDS
    // =========================================================

    private static final Set<String>
            ALLOWED_SORT_FIELDS =
            Set.of(
                    "title",
                    "status",
                    "priority",
                    "dueDate",
                    "createdDate",
                    "updatedDate"
            );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskController(

            TaskService taskService,

            ProjectService projectService) {

        this.taskService =
                taskService;

        this.projectService =
                projectService;
    }

    // =========================================================
    // TASK STATISTICS
    //
    // GET /api/tasks/stats
    // =========================================================

    @GetMapping("/tasks/stats")
    public ResponseEntity<Map<String, Long>>
            getTaskStats(

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        Map<String, Long> stats =
                projectService
                        .getProjectStats(
                                email
                        );

        return ResponseEntity.ok(
                stats
        );
    }

    // =========================================================
    // FULL PROJECT TASK LIST
    //
    // GET /api/projects/{projectId}/tasks
    //
    // Still temporarily used for project-health calculations.
    // =========================================================

    @GetMapping(
            "/projects/{projectId}/tasks"
    )
    public ResponseEntity<List<TaskResponse>>
            getTasks(

                    @PathVariable
                    Long projectId,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        List<TaskResponse> tasks =
                taskService
                        .getTasksByProject(

                                projectId,

                                email
                        );

        return ResponseEntity.ok(
                tasks
        );
    }

    // =========================================================
    // PAGINATED / FILTERED TASK LIST
    //
    // GET /api/projects/{projectId}/tasks/page
    // =========================================================

    @GetMapping(
            "/projects/{projectId}/tasks/page"
    )
    public ResponseEntity<Page<TaskResponse>>
            getTasksPaged(

                    @PathVariable
                    Long projectId,

                    @RequestParam(
                        defaultValue = "0"
                    )
                    int page,

                    @RequestParam(
                        defaultValue = "10"
                    )
                    int size,

                    @RequestParam(
                        required = false
                    )
                    String status,

                    @RequestParam(
                        required = false
                    )
                    String priority,

                    @RequestParam(
                        required = false
                    )
                    String search,

                    @RequestParam(
                        required = false
                    )
                    String dueDateFilter,

                    @RequestParam(
                        defaultValue = "dueDate"
                    )
                    String sortBy,

                    @RequestParam(
                        defaultValue = "asc"
                    )
                    String sortDirection,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        validatePaging(
                page,
                size
        );

        String safeSortField =
                validateSortField(
                        sortBy
                );

        Sort.Direction direction =
                parseSortDirection(
                        sortDirection
                );

        Sort sort =
                Sort.by(

                        direction,

                        safeSortField
                );

        Pageable pageable =
                PageRequest.of(

                        page,

                        size,

                        sort
                );

        Page<TaskResponse> taskPage =
                taskService
                        .getTasksByProjectPaged(

                                projectId,

                                email,

                                status,

                                priority,

                                search,

                                dueDateFilter,

                                pageable
                        );

        return ResponseEntity.ok(
                taskPage
        );
    }

    // =========================================================
    // CREATE TASK
    //
    // POST /api/projects/{projectId}/tasks
    // =========================================================

    @PostMapping(
            "/projects/{projectId}/tasks"
    )
    public ResponseEntity<TaskResponse>
            createTask(

                    @PathVariable
                    Long projectId,

                    @Valid
                    @RequestBody
                    TaskRequest request,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        TaskResponse createdTask =
                taskService
                        .createTask(

                                projectId,

                                request,

                                email
                        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        createdTask
                );
    }

    // =========================================================
    // UPDATE TASK
    //
    // PUT /api/projects/{projectId}/tasks/{taskId}
    // =========================================================

    @PutMapping(
            "/projects/{projectId}/tasks/{taskId}"
    )
    public ResponseEntity<TaskResponse>
            updateTask(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    @Valid
                    @RequestBody
                    TaskRequest request,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        TaskResponse updatedTask =
                taskService
                        .updateTask(

                                projectId,

                                taskId,

                                request,

                                email
                        );

        return ResponseEntity.ok(
                updatedTask
        );
    }

    // =========================================================
    // DELETE TASK
    //
    // DELETE /api/projects/{projectId}/tasks/{taskId}
    // =========================================================

    @DeleteMapping(
            "/projects/{projectId}/tasks/{taskId}"
    )
    public ResponseEntity<Void>
            deleteTask(

                    @PathVariable
                    Long projectId,

                    @PathVariable
                    Long taskId,

                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        taskService.deleteTask(

                projectId,

                taskId,

                email
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =========================================================
    // PAGINATION VALIDATION
    // =========================================================

    private void validatePaging(

            int page,

            int size) {

        if (
            page < 0
        ) {

            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (
            size < 1
            || size > 100
        ) {

            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }
    }

    // =========================================================
    // SORT FIELD VALIDATION
    // =========================================================

    private String validateSortField(
            String sortBy) {

        if (
            sortBy == null
            || sortBy.isBlank()
        ) {

            return "dueDate";
        }

        String normalized =
                sortBy.trim();

        if (
            !ALLOWED_SORT_FIELDS
                    .contains(
                            normalized
                    )
        ) {

            throw new IllegalArgumentException(
                    "Invalid task sort field: "
                            + sortBy
            );
        }

        return normalized;
    }

    // =========================================================
    // SORT DIRECTION
    // =========================================================

    private Sort.Direction
            parseSortDirection(

                    String sortDirection) {

        if (
            sortDirection == null
            || sortDirection.isBlank()
        ) {

            return Sort.Direction.ASC;
        }

        if (
            "asc".equalsIgnoreCase(
                    sortDirection.trim()
            )
        ) {

            return Sort.Direction.ASC;
        }

        if (
            "desc".equalsIgnoreCase(
                    sortDirection.trim()
            )
        ) {

            return Sort.Direction.DESC;
        }

        throw new IllegalArgumentException(
                "Sort direction must be asc or desc."
        );
    }

    // =========================================================
    // AUTHENTICATION HELPER
    // =========================================================

    private String getAuthenticatedEmail(
            Principal principal) {

        if (
            principal == null
            || principal.getName() == null
            || principal
                    .getName()
                    .isBlank()
        ) {

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