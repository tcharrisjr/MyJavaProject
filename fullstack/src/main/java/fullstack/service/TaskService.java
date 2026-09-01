package fullstack.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.task.TaskRequest;
import fullstack.dto.task.TaskResponse;
import fullstack.model.ActivityType;
import fullstack.model.AppUser;
import fullstack.model.Label;
import fullstack.model.Project;
import fullstack.model.Task;
import fullstack.repository.LabelRepository;
import fullstack.repository.ProjectRepository;
import fullstack.repository.TaskRepository;
import fullstack.repository.UserRepository;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final LabelRepository labelRepository;

    /*
     * Sequence 15A
     *
     * Central service used to create project activity /
     * audit-history records.
     */
    private final ProjectActivityService
            projectActivityService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            LabelRepository labelRepository,
            ProjectActivityService projectActivityService) {

        this.taskRepository =
                taskRepository;

        this.projectRepository =
                projectRepository;

        this.userRepository =
                userRepository;

        this.labelRepository =
                labelRepository;

        this.projectActivityService =
                projectActivityService;
    }

    // =========================================================
    // GET ALL TASKS FOR PROJECT
    // =========================================================

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(
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
    public Page<TaskResponse> getTasksByProjectPaged(
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
         * Project relationship is controlled by the server.
         */
        task.setProject(
                project
        );

        /*
         * Sequence 13A - Assignee
         *
         * null means the task remains unassigned.
         */
        task.setAssignee(
                resolveAssignee(
                        request.getAssigneeId()
                )
        );

        /*
         * Sequence 13B - Labels
         */
        task.setLabels(
                resolveLabels(
                        request.getLabels()
                )
        );

        Task savedTask =
                taskRepository.save(
                        task
                );

        /*
         * Sequence 15A - Task Activity
         *
         * A newly created task produces one TASK_CREATED
         * activity event.
         */
        projectActivityService
                .recordTaskActivity(
                        project,
                        savedTask,
                        email,
                        ActivityType.TASK_CREATED,
                        "Created task \""
                                + savedTask.getTitle()
                                + "\""
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

        /*
         * =====================================================
         * SEQUENCE 15A
         * CAPTURE ORIGINAL VALUES
         * =====================================================
         *
         * These values must be captured BEFORE the Task entity
         * is modified.
         */

        String oldTitle =
                existingTask.getTitle();

        String oldDescription =
                existingTask.getDescription();

        String oldStatus =
                existingTask.getStatus();

        String oldPriority =
                existingTask.getPriority();

        LocalDate oldDueDate =
                existingTask.getDueDate();

        AppUser oldAssignee =
                existingTask.getAssignee();

        String oldLabels =
                labelNames(
                        existingTask.getLabels()
                );

        /*
         * Normalize / resolve the incoming values once.
         *
         * This gives us both:
         *
         * 1. values to place onto the Task
         * 2. values to compare against the original snapshot
         */

        String newTitle =
                normalizeTitle(
                        request.getTitle()
                );

        String newDescription =
                normalizeDescription(
                        request.getDescription()
                );

        String newStatus =
                normalizeStatus(
                        request.getStatus()
                );

        String newPriority =
                normalizePriority(
                        request.getPriority()
                );

        LocalDate newDueDate =
                request.getDueDate();

        AppUser newAssignee =
                resolveAssignee(
                        request.getAssigneeId()
                );

        Set<Label> newLabels =
                resolveLabels(
                        request.getLabels()
                );

        String newLabelNames =
                labelNames(
                        newLabels
                );

        /*
         * Apply the normalized values.
         */

        existingTask.setTitle(
                newTitle
        );

        existingTask.setDescription(
                newDescription
        );

        existingTask.setStatus(
                newStatus
        );

        existingTask.setPriority(
                newPriority
        );

        existingTask.setDueDate(
                newDueDate
        );

        existingTask.setAssignee(
                newAssignee
        );

        existingTask.setLabels(
                newLabels
        );

        Task savedTask =
                taskRepository.save(
                        existingTask
                );

        Project project =
                savedTask.getProject();

        /*
         * =====================================================
         * TITLE CHANGE
         * =====================================================
         */

        if (!Objects.equals(
                oldTitle,
                newTitle)) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            savedTask,
                            email,
                            ActivityType.TASK_UPDATED,
                            "title",
                            oldTitle,
                            newTitle,
                            "Changed task title from \""
                                    + oldTitle
                                    + "\" to \""
                                    + newTitle
                                    + "\""
                    );
        }

        /*
         * =====================================================
         * DESCRIPTION CHANGE
         * =====================================================
         */

        if (!Objects.equals(
                oldDescription,
                newDescription)) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            savedTask,
                            email,
                            ActivityType.TASK_UPDATED,
                            "description",
                            oldDescription,
                            newDescription,
                            "Updated description for task \""
                                    + savedTask.getTitle()
                                    + "\""
                    );
        }

        /*
         * =====================================================
         * STATUS CHANGE
         * =====================================================
         */

        if (!Objects.equals(
                oldStatus,
                newStatus)) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            savedTask,
                            email,
                            ActivityType.TASK_STATUS_CHANGED,
                            "status",
                            oldStatus,
                            newStatus,
                            "Changed task status from "
                                    + displayValue(oldStatus)
                                    + " to "
                                    + displayValue(newStatus)
                                    + " for \""
                                    + savedTask.getTitle()
                                    + "\""
                    );
        }

        /*
         * =====================================================
         * PRIORITY CHANGE
         * =====================================================
         */

        if (!Objects.equals(
                oldPriority,
                newPriority)) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            savedTask,
                            email,
                            ActivityType.TASK_UPDATED,
                            "priority",
                            oldPriority,
                            newPriority,
                            "Changed task priority from "
                                    + displayValue(oldPriority)
                                    + " to "
                                    + displayValue(newPriority)
                                    + " for \""
                                    + savedTask.getTitle()
                                    + "\""
                    );
        }

        /*
         * =====================================================
         * DUE DATE CHANGE
         * =====================================================
         */

        if (!Objects.equals(
                oldDueDate,
                newDueDate)) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            savedTask,
                            email,
                            ActivityType.TASK_UPDATED,
                            "dueDate",
                            dateValue(
                                    oldDueDate
                            ),
                            dateValue(
                                    newDueDate
                            ),
                            "Changed due date for task \""
                                    + savedTask.getTitle()
                                    + "\""
                    );
        }

        /*
         * =====================================================
         * ASSIGNEE CHANGE
         * =====================================================
         */

        recordAssigneeChange(
                project,
                savedTask,
                email,
                oldAssignee,
                newAssignee
        );

        /*
         * =====================================================
         * LABEL CHANGE
         * =====================================================
         */

        if (!Objects.equals(
                oldLabels,
                newLabelNames)) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            savedTask,
                            email,
                            ActivityType.TASK_LABELS_CHANGED,
                            "labels",
                            oldLabels,
                            newLabelNames,
                            "Updated labels for task \""
                                    + savedTask.getTitle()
                                    + "\""
                    );
        }

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

        Project project =
                task.getProject();

        /*
         * Sequence 15A
         *
         * Record the deletion BEFORE deleting the task.
         *
         * V7 uses:
         *
         * ON DELETE SET NULL
         *
         * for project_activity.task_id, so the activity record
         * remains after the task itself is removed.
         *
         * The task ID is also included in the description so
         * the history remains meaningful after task_id becomes
         * NULL.
         */

        projectActivityService
                .recordTaskActivity(
                        project,
                        task,
                        email,
                        ActivityType.TASK_DELETED,
                        "Deleted task \""
                                + task.getTitle()
                                + "\" (task #"
                                + task.getId()
                                + ")"
                );

        taskRepository.delete(
                task
        );
    }

    // =========================================================
    // SEQUENCE 15A - RECORD ASSIGNEE CHANGE
    // =========================================================

    private void recordAssigneeChange(
            Project project,
            Task task,
            String email,
            AppUser oldAssignee,
            AppUser newAssignee) {

        Long oldAssigneeId =
                oldAssignee == null
                        ? null
                        : oldAssignee.getId();

        Long newAssigneeId =
                newAssignee == null
                        ? null
                        : newAssignee.getId();

        /*
         * No assignment change.
         */
        if (Objects.equals(
                oldAssigneeId,
                newAssigneeId)) {

            return;
        }

        String oldValue =
                assigneeValue(
                        oldAssignee
                );

        String newValue =
                assigneeValue(
                        newAssignee
                );

        /*
         * Unassigned -> Assigned
         */
        if (oldAssignee == null
                && newAssignee != null) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            task,
                            email,
                            ActivityType.TASK_ASSIGNED,
                            "assignee",
                            null,
                            newValue,
                            "Assigned task \""
                                    + task.getTitle()
                                    + "\" to "
                                    + newValue
                    );

            return;
        }

        /*
         * Assigned -> Unassigned
         */
        if (oldAssignee != null
                && newAssignee == null) {

            projectActivityService
                    .recordTaskFieldChange(
                            project,
                            task,
                            email,
                            ActivityType.TASK_UNASSIGNED,
                            "assignee",
                            oldValue,
                            null,
                            "Unassigned "
                                    + oldValue
                                    + " from task \""
                                    + task.getTitle()
                                    + "\""
                    );

            return;
        }

        /*
         * Assigned user A -> Assigned user B
         */
        projectActivityService
                .recordTaskFieldChange(
                        project,
                        task,
                        email,
                        ActivityType.TASK_ASSIGNED,
                        "assignee",
                        oldValue,
                        newValue,
                        "Reassigned task \""
                                + task.getTitle()
                                + "\" from "
                                + oldValue
                                + " to "
                                + newValue
                );
    }

    // =========================================================
    // ASSIGNEE LOOKUP
    // =========================================================

    private AppUser resolveAssignee(
            Long assigneeId) {

        if (assigneeId == null) {

            return null;
        }

        return userRepository
                .findById(
                        assigneeId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Assignee not found."
                                )
                );
    }

    // =========================================================
    // SEQUENCE 15A - ASSIGNEE DISPLAY VALUE
    // =========================================================

    private String assigneeValue(
            AppUser user) {

        if (user == null) {

            return null;
        }

        String name =
                user.getName();

        String email =
                user.getEmail();

        if (name != null
                && !name.isBlank()
                && email != null
                && !email.isBlank()) {

            return name.trim()
                    + " <"
                    + email.trim()
                    + ">";
        }

        if (name != null
                && !name.isBlank()) {

            return name.trim();
        }

        if (email != null
                && !email.isBlank()) {

            return email.trim();
        }

        if (user.getId() != null) {

            return "User #"
                    + user.getId();
        }

        return "Unknown user";
    }

    // =========================================================
    // SEQUENCE 13B - LABEL RESOLUTION
    // =========================================================

    private Set<Label> resolveLabels(
            Set<String> labelNames) {

        Set<Label> resolvedLabels =
                new HashSet<>();

        if (labelNames == null
                || labelNames.isEmpty()) {

            return resolvedLabels;
        }

        for (String labelName : labelNames) {

            String normalizedName =
                    normalizeLabelName(
                            labelName
                    );

            if (normalizedName == null) {

                continue;
            }

            Label label =
                    labelRepository
                            .findByNameIgnoreCase(
                                    normalizedName
                            )
                            .orElseGet(
                                    () ->
                                            labelRepository.save(
                                                    new Label(
                                                            normalizedName
                                                    )
                                            )
                            );

            resolvedLabels.add(
                    label
            );
        }

        return resolvedLabels;
    }

    // =========================================================
    // SEQUENCE 15A - STABLE LABEL SNAPSHOT
    // =========================================================

    private String labelNames(
            Set<Label> labels) {

        if (labels == null
                || labels.isEmpty()) {

            return null;
        }

        String value =
                labels.stream()
                        .filter(
                                Objects::nonNull
                        )
                        .map(
                                Label::getName
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .map(
                                String::trim
                        )
                        .filter(
                                name ->
                                        !name.isEmpty()
                        )
                        .sorted(
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .collect(
                                Collectors.joining(
                                        ", "
                                )
                        );

        return value.isBlank()
                ? null
                : value;
    }

    // =========================================================
    // SEQUENCE 13B - LABEL NAME NORMALIZATION
    // =========================================================

    private String normalizeLabelName(
            String labelName) {

        if (labelName == null
                || labelName.isBlank()) {

            return null;
        }

        String normalized =
                labelName.trim();

        if (normalized.length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Label name cannot exceed 100 characters."
            );
        }

        return normalized;
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private TaskResponse toResponse(
            Task task) {

        TaskResponse response =
                new TaskResponse();

        response.setId(
                task.getId()
        );

        if (task.getProject() != null) {

            response.setProjectId(
                    task.getProject()
                            .getId()
            );
        }

        response.setTitle(
                task.getTitle()
        );

        response.setDescription(
                task.getDescription()
        );

        response.setStatus(
                task.getStatus()
        );

        response.setPriority(
                task.getPriority()
        );

        response.setDueDate(
                task.getDueDate()
        );

        response.setCreatedDate(
                task.getCreatedDate()
        );

        response.setUpdatedDate(
                task.getUpdatedDate()
        );

        /*
         * Sequence 13A - Assignee
         */
        if (task.getAssignee() != null) {

            response.setAssigneeId(
                    task.getAssignee()
                            .getId()
            );

            response.setAssigneeName(
                    task.getAssignee()
                            .getName()
            );

            response.setAssigneeEmail(
                    task.getAssignee()
                            .getEmail()
            );
        }

        /*
         * Sequence 13B - Labels
         */
        if (task.getLabels() != null) {

            Set<String> labelNames =
                    new HashSet<>();

            for (Label label :
                    task.getLabels()) {

                if (label != null
                        && label.getName() != null) {

                    labelNames.add(
                            label.getName()
                    );
                }
            }

            response.setLabels(
                    labelNames
            );
        }

        return response;
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

        if (title == null
                || title.isBlank()) {

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

        if (description == null) {

            return "";
        }

        return description.trim();
    }

    // =========================================================
    // STATUS NORMALIZATION
    // =========================================================

    private String normalizeStatus(
            String status) {

        if (status == null
                || status.isBlank()) {

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

        return switch (normalized) {

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

        if (status == null
                || status.isBlank()
                || "ALL".equalsIgnoreCase(
                        status.trim()
                )) {

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

        if (priority == null
                || priority.isBlank()) {

            return "MEDIUM";
        }

        String normalized =
                priority
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (normalized) {

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

        if (priority == null
                || priority.isBlank()
                || "ALL".equalsIgnoreCase(
                        priority.trim()
                )) {

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

        if (search == null
                || search.isBlank()) {

            return null;
        }

        return search.trim();
    }

    // =========================================================
    // DUE DATE FILTER NORMALIZATION
    // =========================================================

    private String normalizeDueDateFilter(
            String dueDateFilter) {

        if (dueDateFilter == null
                || dueDateFilter.isBlank()
                || "ALL".equalsIgnoreCase(
                        dueDateFilter.trim()
                )) {

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

        return switch (normalized) {

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

    // =========================================================
    // SEQUENCE 15A - DATE VALUE
    // =========================================================

    private String dateValue(
            LocalDate date) {

        return date == null
                ? null
                : date.toString();
    }

    // =========================================================
    // SEQUENCE 15A - DISPLAY VALUE
    // =========================================================

    private String displayValue(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "(none)";
        }

        return value
                .trim()
                .replace(
                        '_',
                        ' '
                );
    }
}