package fullstack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
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

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String EMAIL =
            "test@example.com";

    // =========================================================
    // MOCKS
    // =========================================================

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LabelRepository labelRepository;

    /*
     * Sequence 15A
     *
     * TaskService now records task activity through
     * ProjectActivityService.
     */
    @Mock
    private ProjectActivityService projectActivityService;

    // =========================================================
    // SERVICE
    // =========================================================

    private TaskService taskService;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        taskService =
                new TaskService(
                        taskRepository,
                        projectRepository,
                        userRepository,
                        labelRepository,
                        projectActivityService
                );
    }

    // =========================================================
    // GET PROJECT TASKS
    // =========================================================

    @Test
    void getTasksByProject_whenProjectOwned_returnsTasks() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Test Task",
                        "OPEN",
                        "HIGH"
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository
                        .findTasksForOwnedProject(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                List.of(
                        task
                )
        );

        List<TaskResponse> result =
                taskService
                        .getTasksByProject(
                                1L,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                10L,
                result.get(0).getId()
        );

        assertEquals(
                1L,
                result.get(0).getProjectId()
        );

        assertEquals(
                "Test Task",
                result.get(0).getTitle()
        );

        assertEquals(
                "OPEN",
                result.get(0).getStatus()
        );

        assertEquals(
                "HIGH",
                result.get(0).getPriority()
        );

        assertNull(
                result.get(0).getAssigneeId()
        );
    }

    // =========================================================
    // GET PROJECT TASKS - PROJECT NOT OWNED
    // =========================================================

    @Test
    void getTasksByProject_whenProjectNotOwned_throws404() {

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskService
                                        .getTasksByProject(
                                                1L,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Project not found.",
                exception.getReason()
        );

        verify(
                taskRepository,
                never()
        )
        .findTasksForOwnedProject(
                1L,
                EMAIL
        );
    }

    // =========================================================
    // GET PROJECT TASKS - ASSIGNEE RESPONSE
    // =========================================================

    @Test
    void getTasksByProject_whenTaskAssigned_returnsAssigneeFields() {

        Project project =
                createProject(
                        1L
                );

        AppUser assignee =
                createUser(
                        5L,
                        "Test User",
                        "assignee@example.com"
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Assigned Task",
                        "OPEN",
                        "HIGH"
                );

        task.setAssignee(
                assignee
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository
                        .findTasksForOwnedProject(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                List.of(
                        task
                )
        );

        TaskResponse result =
                taskService
                        .getTasksByProject(
                                1L,
                                EMAIL
                        )
                        .get(0);

        assertEquals(
                5L,
                result.getAssigneeId()
        );

        assertEquals(
                "Test User",
                result.getAssigneeName()
        );

        assertEquals(
                "assignee@example.com",
                result.getAssigneeEmail()
        );
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    @Test
    void createTask_normalizesFieldsAndAssignsProject() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "   Build Dashboard   "
        );

        request.setDescription(
                "   Build React dashboard   "
        );

        request.setStatus(
                "in progress"
        );

        request.setPriority(
                " high "
        );

        request.setDueDate(
                LocalDate.of(
                        2026,
                        8,
                        30
                )
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository.save(
                        any(Task.class)
                )
        )
        .thenAnswer(
                invocation -> {

                    Task task =
                            invocation.getArgument(
                                    0
                            );

                    task.setId(
                            10L
                    );

                    task.setCreatedDate(
                            LocalDateTime.now()
                    );

                    task.setUpdatedDate(
                            LocalDateTime.now()
                    );

                    return task;
                }
        );

        TaskResponse result =
                taskService
                        .createTask(
                                1L,
                                request,
                                EMAIL
                        );

        assertEquals(
                10L,
                result.getId()
        );

        assertEquals(
                1L,
                result.getProjectId()
        );

        assertEquals(
                "Build Dashboard",
                result.getTitle()
        );

        assertEquals(
                "Build React dashboard",
                result.getDescription()
        );

        assertEquals(
                "IN_PROGRESS",
                result.getStatus()
        );

        assertEquals(
                "HIGH",
                result.getPriority()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        8,
                        30
                ),
                result.getDueDate()
        );

        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(
                        Task.class
                );

        verify(
                taskRepository
        )
        .save(
                taskCaptor.capture()
        );

        Task savedTask =
                taskCaptor.getValue();

        assertSame(
                project,
                savedTask.getProject()
        );

        assertEquals(
                "Build Dashboard",
                savedTask.getTitle()
        );

        assertEquals(
                "IN_PROGRESS",
                savedTask.getStatus()
        );

        assertEquals(
                "HIGH",
                savedTask.getPriority()
        );
    }

    // =========================================================
    // SEQUENCE 15A - CREATE TASK ACTIVITY
    // =========================================================

    @Test
    void createTask_recordsTaskCreatedActivity() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                createRequest(
                        "New Task",
                        "Description",
                        "OPEN",
                        "MEDIUM"
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository.save(
                        any(Task.class)
                )
        )
        .thenAnswer(
                invocation -> {

                    Task task =
                            invocation.getArgument(
                                    0
                            );

                    task.setId(
                            10L
                    );

                    return task;
                }
        );

        taskService.createTask(
                1L,
                request,
                EMAIL
        );

        verify(
                projectActivityService
        )
        .recordTaskActivity(
                eq(project),
                any(Task.class),
                eq(EMAIL),
                eq(ActivityType.TASK_CREATED),
                eq("Created task \"New Task\"")
        );
    }

    // =========================================================
    // CREATE TASK - ASSIGNEE
    // =========================================================

    @Test
    void createTask_whenAssigneeProvided_assignsUser() {

        Project project =
                createProject(
                        1L
                );

        AppUser assignee =
                createUser(
                        5L,
                        "Assigned User",
                        "assigned@example.com"
                );

        TaskRequest request =
                createRequest(
                        "Assigned Task",
                        "Test",
                        "OPEN",
                        "MEDIUM"
                );

        request.setAssigneeId(
                5L
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                userRepository.findById(
                        5L
                )
        )
        .thenReturn(
                Optional.of(
                        assignee
                )
        );

        when(
                taskRepository.save(
                        any(Task.class)
                )
        )
        .thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        TaskResponse result =
                taskService
                        .createTask(
                                1L,
                                request,
                                EMAIL
                        );

        assertEquals(
                5L,
                result.getAssigneeId()
        );

        assertEquals(
                "Assigned User",
                result.getAssigneeName()
        );

        assertEquals(
                "assigned@example.com",
                result.getAssigneeEmail()
        );

        verify(
                userRepository
        )
        .findById(
                5L
        );
    }

    // =========================================================
    // CREATE TASK - INVALID ASSIGNEE
    // =========================================================

    @Test
    void createTask_whenAssigneeDoesNotExist_throws400() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                createRequest(
                        "Invalid Assignee",
                        "",
                        "OPEN",
                        "MEDIUM"
                );

        request.setAssigneeId(
                999L
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                userRepository.findById(
                        999L
                )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskService
                                        .createTask(
                                                1L,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Assignee not found.",
                exception.getReason()
        );

        verify(
                taskRepository,
                never()
        )
        .save(
                any(Task.class)
        );

        verify(
                projectActivityService,
                never()
        )
        .recordTaskActivity(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // CREATE TASK - DEFAULT STATUS / PRIORITY
    // =========================================================

    @Test
    void createTask_whenStatusAndPriorityNull_usesDefaults() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Default Test"
        );

        request.setStatus(
                null
        );

        request.setPriority(
                null
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository.save(
                        any(Task.class)
                )
        )
        .thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        TaskResponse result =
                taskService
                        .createTask(
                                1L,
                                request,
                                EMAIL
                        );

        assertEquals(
                "OPEN",
                result.getStatus()
        );

        assertEquals(
                "MEDIUM",
                result.getPriority()
        );
    }

    // =========================================================
    // CREATE TASK - INVALID STATUS
    // =========================================================

    @Test
    void createTask_whenStatusInvalid_throwsIllegalArgumentException() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                createRequest(
                        "Invalid Status",
                        "",
                        "WAITING",
                        "MEDIUM"
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                taskService
                                        .createTask(
                                                1L,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                "Invalid task status: WAITING",
                exception.getMessage()
        );

        verify(
                taskRepository,
                never()
        )
        .save(
                any(Task.class)
        );
    }

    // =========================================================
    // CREATE TASK - INVALID PRIORITY
    // =========================================================

    @Test
    void createTask_whenPriorityInvalid_throwsIllegalArgumentException() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                createRequest(
                        "Invalid Priority",
                        "",
                        "OPEN",
                        "CRITICAL"
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                taskService
                                        .createTask(
                                                1L,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                "Invalid task priority: CRITICAL",
                exception.getMessage()
        );

        verify(
                taskRepository,
                never()
        )
        .save(
                any(Task.class)
        );
    }

    // =========================================================
    // CREATE TASK - BLANK TITLE
    // =========================================================

    @Test
    void createTask_whenTitleBlank_throwsIllegalArgumentException() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "   "
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                taskService
                                        .createTask(
                                                1L,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                "Task title is required.",
                exception.getMessage()
        );

        verify(
                taskRepository,
                never()
        )
        .save(
                any(Task.class)
        );
    }

    // =========================================================
    // CREATE TASK - PROJECT NOT OWNED
    // =========================================================

    @Test
    void createTask_whenProjectNotOwned_throws404() {

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Test Task"
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskService
                                        .createTask(
                                                1L,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(
                taskRepository,
                never()
        )
        .save(
                any(Task.class)
        );
    }

    // =========================================================
    // CREATE TASK - LABELS
    // =========================================================

    @Test
    void createTask_whenLabelsProvided_resolvesLabels() {

        Project project =
                createProject(
                        1L
                );

        Label backend =
                new Label(
                        "Backend"
                );

        TaskRequest request =
                createRequest(
                        "Labels",
                        "",
                        "OPEN",
                        "MEDIUM"
                );

        request.setLabels(
                Set.of(
                        "Backend"
                )
        );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                labelRepository
                        .findByNameIgnoreCase(
                                "Backend"
                        )
        )
        .thenReturn(
                Optional.of(
                        backend
                )
        );

        when(
                taskRepository.save(
                        any(Task.class)
                )
        )
        .thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        TaskResponse result =
                taskService
                        .createTask(
                                1L,
                                request,
                                EMAIL
                        );

        assertEquals(
                Set.of(
                        "Backend"
                ),
                result.getLabels()
        );
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @Test
    void updateTask_whenOwned_updatesTask() {

        Project project =
                createProject(
                        1L
                );

        Task existingTask =
                createTask(
                        10L,
                        project,
                        "Old Title",
                        "OPEN",
                        "LOW"
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                " Updated Title "
        );

        request.setDescription(
                " Updated Description "
        );

        request.setStatus(
                "completed"
        );

        request.setPriority(
                "high"
        );

        request.setDueDate(
                LocalDate.of(
                        2026,
                        9,
                        1
                )
        );

        whenOwnedTask(
                existingTask
        );

        when(
                taskRepository.save(
                        existingTask
                )
        )
        .thenReturn(
                existingTask
        );

        TaskResponse result =
                taskService
                        .updateTask(
                                1L,
                                10L,
                                request,
                                EMAIL
                        );

        assertEquals(
                "Updated Title",
                result.getTitle()
        );

        assertEquals(
                "Updated Description",
                result.getDescription()
        );

        assertEquals(
                "COMPLETED",
                result.getStatus()
        );

        assertEquals(
                "HIGH",
                result.getPriority()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        9,
                        1
                ),
                result.getDueDate()
        );

        assertNull(
                result.getAssigneeId()
        );
    }

    // =========================================================
    // SEQUENCE 15A - UPDATE TITLE ACTIVITY
    // =========================================================

    @Test
    void updateTask_whenTitleChanges_recordsTaskUpdatedActivity() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Old Title",
                        "OPEN",
                        "MEDIUM"
                );

        TaskRequest request =
                requestMatchingTask(
                        task
                );

        request.setTitle(
                "New Title"
        );

        whenOwnedTask(
                task
        );

        when(
                taskRepository.save(
                        task
                )
        )
        .thenReturn(
                task
        );

        taskService.updateTask(
                1L,
                10L,
                request,
                EMAIL
        );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.TASK_UPDATED,
                "title",
                "Old Title",
                "New Title",
                "Changed task title from \"Old Title\" to \"New Title\""
        );
    }

    // =========================================================
    // SEQUENCE 15A - STATUS ACTIVITY
    // =========================================================

    @Test
    void updateTask_whenStatusChanges_recordsStatusActivity() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Status Task",
                        "OPEN",
                        "MEDIUM"
                );

        TaskRequest request =
                requestMatchingTask(
                        task
                );

        request.setStatus(
                "COMPLETED"
        );

        whenOwnedTask(
                task
        );

        when(
                taskRepository.save(
                        task
                )
        )
        .thenReturn(
                task
        );

        taskService.updateTask(
                1L,
                10L,
                request,
                EMAIL
        );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.TASK_STATUS_CHANGED,
                "status",
                "OPEN",
                "COMPLETED",
                "Changed task status from OPEN to COMPLETED for \"Status Task\""
        );
    }

    // =========================================================
    // SEQUENCE 15A - NO CHANGE
    // =========================================================

    @Test
    void updateTask_whenNothingChanges_doesNotRecordActivity() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "No Change",
                        "OPEN",
                        "MEDIUM"
                );

        TaskRequest request =
                requestMatchingTask(
                        task
                );

        whenOwnedTask(
                task
        );

        when(
                taskRepository.save(
                        task
                )
        )
        .thenReturn(
                task
        );

        taskService.updateTask(
                1L,
                10L,
                request,
                EMAIL
        );

        verify(
                projectActivityService,
                never()
        )
        .recordTaskFieldChange(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // UPDATE TASK - ASSIGN USER
    // =========================================================

    @Test
    void updateTask_whenAssigneeProvided_assignsUser() {

        Project project =
                createProject(
                        1L
                );

        Task existingTask =
                createTask(
                        10L,
                        project,
                        "Assigned Task",
                        "OPEN",
                        "LOW"
                );

        AppUser assignee =
                createUser(
                        5L,
                        "Assigned User",
                        "assigned@example.com"
                );

        TaskRequest request =
                requestMatchingTask(
                        existingTask
                );

        request.setAssigneeId(
                5L
        );

        whenOwnedTask(
                existingTask
        );

        when(
                userRepository.findById(
                        5L
                )
        )
        .thenReturn(
                Optional.of(
                        assignee
                )
        );

        when(
                taskRepository.save(
                        existingTask
                )
        )
        .thenReturn(
                existingTask
        );

        TaskResponse result =
                taskService
                        .updateTask(
                                1L,
                                10L,
                                request,
                                EMAIL
                        );

        assertSame(
                assignee,
                existingTask.getAssignee()
        );

        assertEquals(
                5L,
                result.getAssigneeId()
        );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                existingTask,
                EMAIL,
                ActivityType.TASK_ASSIGNED,
                "assignee",
                null,
                "Assigned User <assigned@example.com>",
                "Assigned task \"Assigned Task\" to Assigned User <assigned@example.com>"
        );
    }

    // =========================================================
    // UPDATE TASK - REMOVE ASSIGNEE
    // =========================================================

    @Test
    void updateTask_whenAssigneeIdNull_removesExistingAssignee() {

        Project project =
                createProject(
                        1L
                );

        Task existingTask =
                createTask(
                        10L,
                        project,
                        "Assigned Task",
                        "OPEN",
                        "MEDIUM"
                );

        existingTask.setAssignee(
                createUser(
                        5L,
                        "Existing User",
                        "existing@example.com"
                )
        );

        TaskRequest request =
                requestMatchingTask(
                        existingTask
                );

        request.setAssigneeId(
                null
        );

        whenOwnedTask(
                existingTask
        );

        when(
                taskRepository.save(
                        existingTask
                )
        )
        .thenReturn(
                existingTask
        );

        TaskResponse result =
                taskService
                        .updateTask(
                                1L,
                                10L,
                                request,
                                EMAIL
                        );

        assertNull(
                existingTask.getAssignee()
        );

        assertNull(
                result.getAssigneeId()
        );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                existingTask,
                EMAIL,
                ActivityType.TASK_UNASSIGNED,
                "assignee",
                "Existing User <existing@example.com>",
                null,
                "Unassigned Existing User <existing@example.com> from task \"Assigned Task\""
        );
    }

    // =========================================================
    // SEQUENCE 15A - REASSIGN USER
    // =========================================================

    @Test
    void updateTask_whenAssigneeChanges_recordsReassignment() {

        Project project =
                createProject(
                        1L
                );

        AppUser oldAssignee =
                createUser(
                        5L,
                        "Old User",
                        "old@example.com"
                );

        AppUser newAssignee =
                createUser(
                        6L,
                        "New User",
                        "new@example.com"
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Reassign Task",
                        "OPEN",
                        "MEDIUM"
                );

        task.setAssignee(
                oldAssignee
        );

        TaskRequest request =
                requestMatchingTask(
                        task
                );

        request.setAssigneeId(
                6L
        );

        whenOwnedTask(
                task
        );

        when(
                userRepository.findById(
                        6L
                )
        )
        .thenReturn(
                Optional.of(
                        newAssignee
                )
        );

        when(
                taskRepository.save(
                        task
                )
        )
        .thenReturn(
                task
        );

        taskService.updateTask(
                1L,
                10L,
                request,
                EMAIL
        );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.TASK_ASSIGNED,
                "assignee",
                "Old User <old@example.com>",
                "New User <new@example.com>",
                "Reassigned task \"Reassign Task\" from Old User <old@example.com> to New User <new@example.com>"
        );
    }

    // =========================================================
    // SEQUENCE 15A - LABEL CHANGE
    // =========================================================

    @Test
    void updateTask_whenLabelsChange_recordsLabelActivity() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Label Task",
                        "OPEN",
                        "MEDIUM"
                );

        Label backend =
                new Label(
                        "Backend"
                );

        task.setLabels(
                new HashSet<>(
                        Set.of(
                                backend
                        )
                )
        );

        Label urgent =
                new Label(
                        "Urgent"
                );

        TaskRequest request =
                requestMatchingTask(
                        task
                );

        request.setLabels(
                Set.of(
                        "Urgent"
                )
        );

        whenOwnedTask(
                task
        );

        when(
                labelRepository
                        .findByNameIgnoreCase(
                                "Urgent"
                        )
        )
        .thenReturn(
                Optional.of(
                        urgent
                )
        );

        when(
                taskRepository.save(
                        task
                )
        )
        .thenReturn(
                task
        );

        taskService.updateTask(
                1L,
                10L,
                request,
                EMAIL
        );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.TASK_LABELS_CHANGED,
                "labels",
                "Backend",
                "Urgent",
                "Updated labels for task \"Label Task\""
        );
    }

    // =========================================================
    // UPDATE TASK - NOT OWNED
    // =========================================================

    @Test
    void updateTask_whenTaskNotOwned_throws404() {

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Updated"
        );

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                10L,
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskService
                                        .updateTask(
                                                1L,
                                                10L,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Task not found.",
                exception.getReason()
        );

        verify(
                taskRepository,
                never()
        )
        .save(
                any(Task.class)
        );

        verify(
                projectActivityService,
                never()
        )
        .recordTaskFieldChange(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    @Test
    void deleteTask_whenOwned_deletesTask() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Delete Me",
                        "OPEN",
                        "MEDIUM"
                );

        whenOwnedTask(
                task
        );

        taskService
                .deleteTask(
                        1L,
                        10L,
                        EMAIL
                );

        verify(
                taskRepository
        )
        .delete(
                task
        );
    }

    // =========================================================
    // SEQUENCE 15A - DELETE TASK ACTIVITY
    // =========================================================

    @Test
    void deleteTask_whenOwned_recordsDeleteActivity() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Delete Me",
                        "OPEN",
                        "MEDIUM"
                );

        whenOwnedTask(
                task
        );

        taskService.deleteTask(
                1L,
                10L,
                EMAIL
        );

        verify(
                projectActivityService
        )
        .recordTaskActivity(
                project,
                task,
                EMAIL,
                ActivityType.TASK_DELETED,
                "Deleted task \"Delete Me\" (task #10)"
        );
    }

    // =========================================================
    // DELETE TASK - NOT OWNED
    // =========================================================

    @Test
    void deleteTask_whenNotOwned_throws404AndDoesNotDelete() {

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                10L,
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        assertThrows(
                ResponseStatusException.class,
                () ->
                        taskService
                                .deleteTask(
                                        1L,
                                        10L,
                                        EMAIL
                                )
        );

        verify(
                taskRepository,
                never()
        )
        .delete(
                any(Task.class)
        );

        verify(
                projectActivityService,
                never()
        )
        .recordTaskActivity(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // PAGINATED FILTERS
    // =========================================================

    @Test
    void getTasksByProjectPaged_normalizesFiltersBeforeRepositoryCall() {

        Project project =
                createProject(
                        1L
                );

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        Page<Task> taskPage =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository
                        .searchTasksForOwnedProject(
                                eq(1L),
                                eq(EMAIL),
                                eq("IN_PROGRESS"),
                                eq("HIGH"),
                                eq("dashboard"),
                                eq("DUE_SOON"),
                                any(LocalDate.class),
                                any(LocalDate.class),
                                eq(pageable)
                        )
        )
        .thenReturn(
                taskPage
        );

        Page<TaskResponse> result =
                taskService
                        .getTasksByProjectPaged(
                                1L,
                                EMAIL,
                                "in progress",
                                "high",
                                " dashboard ",
                                "due soon",
                                pageable
                        );

        assertEquals(
                0,
                result.getTotalElements()
        );

        verify(
                taskRepository
        )
        .searchTasksForOwnedProject(
                eq(1L),
                eq(EMAIL),
                eq("IN_PROGRESS"),
                eq("HIGH"),
                eq("dashboard"),
                eq("DUE_SOON"),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(pageable)
        );
    }

    // =========================================================
    // PAGINATION - ALL FILTERS
    // =========================================================

    @Test
    void getTasksByProjectPaged_whenFiltersAll_passesNullFilters() {

        Project project =
                createProject(
                        1L
                );

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        when(
                taskRepository
                        .searchTasksForOwnedProject(
                                eq(1L),
                                eq(EMAIL),
                                eq(null),
                                eq(null),
                                eq(null),
                                eq(null),
                                any(LocalDate.class),
                                any(LocalDate.class),
                                eq(pageable)
                        )
        )
        .thenReturn(
                Page.empty(
                        pageable
                )
        );

        taskService
                .getTasksByProjectPaged(
                        1L,
                        EMAIL,
                        "ALL",
                        "ALL",
                        "",
                        "ALL",
                        pageable
                );

        verify(
                taskRepository
        )
        .searchTasksForOwnedProject(
                eq(1L),
                eq(EMAIL),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(pageable)
        );
    }

    // =========================================================
    // PAGINATION - INVALID DUE DATE FILTER
    // =========================================================

    @Test
    void getTasksByProjectPaged_whenDueDateFilterInvalid_throwsException() {

        Project project =
                createProject(
                        1L
                );

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                taskService
                                        .getTasksByProjectPaged(
                                                1L,
                                                EMAIL,
                                                "ALL",
                                                "ALL",
                                                "",
                                                "SOMEDAY",
                                                PageRequest.of(
                                                        0,
                                                        10
                                                )
                                        )
                );

        assertEquals(
                "Invalid due date filter: SOMEDAY",
                exception.getMessage()
        );
    }

    // =========================================================
    // ACTIVITY - MULTIPLE FIELD CHANGES
    // =========================================================

    @Test
    void updateTask_whenMultipleFieldsChange_recordsEachChange() {

        Project project =
                createProject(
                        1L
                );

        Task task =
                createTask(
                        10L,
                        project,
                        "Old",
                        "OPEN",
                        "LOW"
                );

        TaskRequest request =
                requestMatchingTask(
                        task
                );

        request.setTitle(
                "New"
        );

        request.setDescription(
                "New Description"
        );

        request.setStatus(
                "COMPLETED"
        );

        request.setPriority(
                "HIGH"
        );

        request.setDueDate(
                LocalDate.of(
                        2026,
                        9,
                        15
                )
        );

        whenOwnedTask(
                task
        );

        when(
                taskRepository.save(
                        task
                )
        )
        .thenReturn(
                task
        );

        taskService.updateTask(
                1L,
                10L,
                request,
                EMAIL
        );

        verify(
                projectActivityService,
                times(5)
        )
        .recordTaskFieldChange(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private Project createProject(
            Long id) {

        Project project =
                new Project();

        project.setId(
                id
        );

        project.setName(
                "Test Project"
        );

        project.setDescription(
                "Test project"
        );

        return project;
    }

    private Task createTask(
            Long id,
            Project project,
            String title,
            String status,
            String priority) {

        Task task =
                new Task();

        task.setId(
                id
        );

        task.setProject(
                project
        );

        task.setTitle(
                title
        );

        task.setDescription(
                "Test Description"
        );

        task.setStatus(
                status
        );

        task.setPriority(
                priority
        );

        task.setDueDate(
                LocalDate.of(
                        2026,
                        8,
                        30
                )
        );

        task.setCreatedDate(
                LocalDateTime.now()
        );

        task.setUpdatedDate(
                LocalDateTime.now()
        );

        task.setLabels(
                new HashSet<>()
        );

        return task;
    }

    private AppUser createUser(
            Long id,
            String name,
            String email) {

        AppUser user =
                new AppUser();

        user.setId(
                id
        );

        user.setName(
                name
        );

        user.setEmail(
                email
        );

        return user;
    }

    private TaskRequest createRequest(
            String title,
            String description,
            String status,
            String priority) {

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                title
        );

        request.setDescription(
                description
        );

        request.setStatus(
                status
        );

        request.setPriority(
                priority
        );

        return request;
    }

    /*
     * Creates an update request that initially contains exactly
     * the same editable values as the supplied task.
     *
     * Individual tests can then change one field and verify
     * exactly one corresponding activity record.
     */
    private TaskRequest requestMatchingTask(
            Task task) {

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                task.getTitle()
        );

        request.setDescription(
                task.getDescription()
        );

        request.setStatus(
                task.getStatus()
        );

        request.setPriority(
                task.getPriority()
        );

        request.setDueDate(
                task.getDueDate()
        );

        if (task.getAssignee() != null) {

            request.setAssigneeId(
                    task.getAssignee()
                            .getId()
            );
        }

        Set<String> labelNames =
                new HashSet<>();

        if (task.getLabels() != null) {

            for (Label label :
                    task.getLabels()) {

                if (label != null
                        && label.getName() != null) {

                    labelNames.add(
                            label.getName()
                    );
                }
            }
        }

        request.setLabels(
                labelNames
        );

        return request;
    }

    private void whenOwnedTask(
            Task task) {

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                task.getId(),
                                task.getProject().getId(),
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        task
                )
        );
    }
}