package fullstack.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.task.TaskRequest;
import fullstack.dto.task.TaskResponse;
import fullstack.model.Project;
import fullstack.model.Task;
import fullstack.repository.ProjectRepository;
import fullstack.repository.TaskRepository;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskService(

            TaskRepository taskRepository,

            ProjectRepository projectRepository) {

        this.taskRepository =
                taskRepository;

        this.projectRepository =
                projectRepository;
    }

    // =========================================================
    // GET ALL TASKS FOR PROJECT
    //
    // Still temporarily used by project-health calculations.
    // =========================================================

    @Transactional(readOnly = true)
    public List<TaskResponse>
            getTasksByProject(

                    Long projectId,

                    String email) {

        verifyProjectOwnership(
                projectId,
                email
        );

        return taskRepository
                .findTasksForOwnedProject(
                        projectId,
                        email
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    // =========================================================
    // PAGINATED / FILTERED TASKS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<TaskResponse>
            getTasksByProjectPaged(

                    Long projectId,

                    String email,

                    String status,

                    String priority,

                    String search,

                    String dueDateFilter,

                    Pageable pageable) {

        verifyProjectOwnership(
                projectId,
                email
        );

        String normalizedStatus =
                normalizeOptionalStatus(
                        status
                );

        String normalizedPriority =
                normalizeOptionalPriority(
                        priority
                );

        String normalizedSearch =
                normalizeSearch(
                        search
                );

        String normalizedDueDateFilter =
                normalizeDueDateFilter(
                        dueDateFilter
                );

        LocalDate today =
                LocalDate.now();

        LocalDate dueSoonEnd =
                today.plusDays(7);

        Page<Task> taskPage =
                taskRepository
                        .searchTasksForOwnedProject(

                                projectId,

                                email,

                                normalizedStatus,

                                normalizedPriority,

                                normalizedSearch,

                                normalizedDueDateFilter,

                                today,

                                dueSoonEnd,

                                pageable
                        );

        return taskPage.map(
                this::toResponse
        );
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    public TaskResponse createTask(

            Long projectId,

            TaskRequest request,

            String email) {

        Project project =
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(

                                projectId,

                                email
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(

                                                HttpStatus.NOT_FOUND,

                                                "Project not found."
                                        )
                        );

        Task task =
                new Task();

        task.setTitle(
                normalizeTitle(
                        request.getTitle()
                )
        );

        task.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );

        task.setStatus(
                normalizeStatus(
                        request.getStatus()
                )
        );

        task.setPriority(
                normalizePriority(
                        request.getPriority()
                )
        );

        task.setDueDate(
                request.getDueDate()
        );

        /*
         * Server controls ownership/project association.
         */
        task.setProject(
                project
        );

        Task savedTask =
                taskRepository.save(
                        task
                );

        return toResponse(
                savedTask
        );
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    public TaskResponse updateTask(

            Long projectId,

            Long taskId,

            TaskRequest request,

            String email) {

        Task existingTask =
                getOwnedTask(

                        projectId,

                        taskId,

                        email
                );

        existingTask.setTitle(
                normalizeTitle(
                        request.getTitle()
                )
        );

        existingTask.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );

        existingTask.setStatus(
                normalizeStatus(
                        request.getStatus()
                )
        );

        existingTask.setPriority(
                normalizePriority(
                        request.getPriority()
                )
        );

        existingTask.setDueDate(
                request.getDueDate()
        );

        Task savedTask =
                taskRepository.save(
                        existingTask
                );

        return toResponse(
                savedTask
        );
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    public void deleteTask(

            Long projectId,

            Long taskId,

            String email) {

        Task task =
                getOwnedTask(

                        projectId,

                        taskId,

                        email
                );

        taskRepository.delete(
                task
        );
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private TaskResponse toResponse(
            Task task) {

        return new TaskResponse(

                task.getId(),

                task.getProjectId(),

                task.getTitle(),

                task.getDescription(),

                task.getStatus(),

                task.getPriority(),

                task.getDueDate(),

                task.getCreatedDate(),

                task.getUpdatedDate()
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
    // PROJECT OWNERSHIP
    // =========================================================

    private void verifyProjectOwnership(

            Long projectId,

            String email) {

        boolean exists =
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(

                                projectId,

                                email
                        )
                        .isPresent();

        if (!exists) {

            /*
             * 404 is intentional.
             *
             * Do not expose whether another user's
             * project exists.
             */

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Project not found."
            );
        }
    }

    // =========================================================
    // TITLE NORMALIZATION
    // =========================================================

    private String normalizeTitle(
            String title) {

        if (
            title == null
            || title.isBlank()
        ) {

            /*
             * Normally intercepted by @NotBlank,
             * but this protects service-layer usage too.
             */

            throw new IllegalArgumentException(
                    "Task title is required."
            );
        }

        return title.trim();
    }

    // =========================================================
    // DESCRIPTION NORMALIZATION
    // =========================================================

    private String normalizeDescription(
            String description) {

        if (
            description == null
        ) {

            return "";
        }

        return description.trim();
    }

    // =========================================================
    // STATUS NORMALIZATION
    // =========================================================

    private String normalizeStatus(
            String status) {

        if (
            status == null
            || status.isBlank()
        ) {

            return "OPEN";
        }

        String normalized =
                status
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
                        .replace(
                                "-",
                                "_"
                        )
                        .replace(
                                " ",
                                "_"
                        );

        return switch (
            normalized
        ) {

            case "OPEN" ->
                    "OPEN";

            case "IN_PROGRESS",
                 "INPROGRESS" ->
                    "IN_PROGRESS";

            case "COMPLETED",
                 "COMPLETE",
                 "DONE" ->
                    "COMPLETED";

            default ->
                    throw new IllegalArgumentException(
                            "Invalid task status: "
                                    + status
                    );
        };
    }

    // =========================================================
    // OPTIONAL STATUS FILTER
    // =========================================================

    private String normalizeOptionalStatus(
            String status) {

        if (
            status == null
            || status.isBlank()
            || "ALL".equalsIgnoreCase(
                    status.trim()
            )
        ) {

            return null;
        }

        return normalizeStatus(
                status
        );
    }

    // =========================================================
    // PRIORITY NORMALIZATION
    // =========================================================

    private String normalizePriority(
            String priority) {

        if (
            priority == null
            || priority.isBlank()
        ) {

            return "MEDIUM";
        }

        String normalized =
                priority
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (
            normalized
        ) {

            case "LOW" ->
                    "LOW";

            case "MEDIUM" ->
                    "MEDIUM";

            case "HIGH" ->
                    "HIGH";

            default ->
                    throw new IllegalArgumentException(
                            "Invalid task priority: "
                                    + priority
                    );
        };
    }

    // =========================================================
    // OPTIONAL PRIORITY FILTER
    // =========================================================

    private String normalizeOptionalPriority(
            String priority) {

        if (
            priority == null
            || priority.isBlank()
            || "ALL".equalsIgnoreCase(
                    priority.trim()
            )
        ) {

            return null;
        }

        return normalizePriority(
                priority
        );
    }

    // =========================================================
    // SEARCH NORMALIZATION
    // =========================================================

    private String normalizeSearch(
            String search) {

        if (
            search == null
            || search.isBlank()
        ) {

            return null;
        }

        return search.trim();
    }

    // =========================================================
    // DUE DATE FILTER NORMALIZATION
    // =========================================================

    private String normalizeDueDateFilter(
            String dueDateFilter) {

        if (
            dueDateFilter == null
            || dueDateFilter.isBlank()
            || "ALL".equalsIgnoreCase(
                    dueDateFilter.trim()
            )
        ) {

            return null;
        }

        String normalized =
                dueDateFilter
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
                        .replace(
                                "-",
                                "_"
                        )
                        .replace(
                                " ",
                                "_"
                        );

        return switch (
            normalized
        ) {

            case "OVERDUE" ->
                    "OVERDUE";

            case "DUE_TODAY",
                 "TODAY" ->
                    "DUE_TODAY";

            case "DUE_SOON" ->
                    "DUE_SOON";

            case "NO_DUE_DATE",
                 "NONE" ->
                    "NO_DUE_DATE";

            default ->
                    throw new IllegalArgumentException(
                            "Invalid due date filter: "
                                    + dueDateFilter
                    );
        };
    }
}