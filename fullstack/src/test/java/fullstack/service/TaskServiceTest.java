package fullstack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import fullstack.model.AppUser;
import fullstack.model.Project;
import fullstack.model.Task;

import fullstack.repository.ProjectRepository;
import fullstack.repository.TaskRepository;
import fullstack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // =========================================================
    // MOCKS
    // =========================================================

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    /*
     * Sequence 13A
     *
     * TaskService now resolves task assignees through
     * UserRepository.
     */
    @Mock
    private UserRepository userRepository;

    // =========================================================
    // SERVICE
    // =========================================================

    private TaskService taskService;

    private static final String EMAIL =
            "test@example.com";

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        taskService =
                new TaskService(
                        taskRepository,
                        projectRepository,
                        userRepository
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

        List<TaskResponse> result =
                taskService
                        .getTasksByProject(
                                1L,
                                EMAIL
                        );

        assertEquals(
                1,
                result.size()
        );

        TaskResponse response =
                result.get(0);

        assertEquals(
                5L,
                response.getAssigneeId()
        );

        assertEquals(
                "Test User",
                response.getAssigneeName()
        );

        assertEquals(
                "assignee@example.com",
                response.getAssigneeEmail()
        );
    }

    // =========================================================
    // PROJECT OWNERSHIP
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

        assertNull(
                result.getAssigneeId()
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

        assertEquals(
                project,
                savedTask.getProject()
        );

        assertEquals(
                "IN_PROGRESS",
                savedTask.getStatus()
        );

        assertEquals(
                "HIGH",
                savedTask.getPriority()
        );

        assertNull(
                savedTask.getAssignee()
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
                new TaskRequest();

        request.setTitle(
                "Assigned Task"
        );

        request.setStatus(
                "OPEN"
        );

        request.setPriority(
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
                assignee,
                savedTask.getAssignee()
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
                new TaskRequest();

        request.setTitle(
                "Invalid Assignee"
        );

        request.setStatus(
                "OPEN"
        );

        request.setPriority(
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
    }

    // =========================================================
    // DEFAULT STATUS / PRIORITY
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
    // STATUS ALIAS
    // =========================================================

    @Test
    void createTask_whenStatusDone_normalizesToCompleted() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Finished Task"
        );

        request.setStatus(
                "done"
        );

        request.setPriority(
                "low"
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
                "COMPLETED",
                result.getStatus()
        );

        assertEquals(
                "LOW",
                result.getPriority()
        );
    }

    // =========================================================
    // INVALID STATUS
    // =========================================================

    @Test
    void createTask_whenStatusInvalid_throwsIllegalArgumentException() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Invalid Status"
        );

        request.setStatus(
                "WAITING"
        );

        request.setPriority(
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
    // INVALID PRIORITY
    // =========================================================

    @Test
    void createTask_whenPriorityInvalid_throwsIllegalArgumentException() {

        Project project =
                createProject(
                        1L
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Invalid Priority"
        );

        request.setStatus(
                "OPEN"
        );

        request.setPriority(
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
    // BLANK TITLE
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

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                10L,
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        existingTask
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
                        "Old Title",
                        "OPEN",
                        "LOW"
                );

        AppUser assignee =
                createUser(
                        5L,
                        "Updated Assignee",
                        "updated@example.com"
                );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Updated Title"
        );

        request.setDescription(
                "Updated Description"
        );

        request.setStatus(
                "IN_PROGRESS"
        );

        request.setPriority(
                "HIGH"
        );

        request.setAssigneeId(
                5L
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
                Optional.of(
                        existingTask
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

        assertEquals(
                "Updated Assignee",
                result.getAssigneeName()
        );

        assertEquals(
                "updated@example.com",
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
                        "Old Title",
                        "OPEN",
                        "LOW"
                );

        existingTask.setAssignee(
                createUser(
                        5L,
                        "Existing User",
                        "existing@example.com"
                )
        );

        TaskRequest request =
                new TaskRequest();

        request.setTitle(
                "Updated Title"
        );

        request.setDescription(
                "Updated"
        );

        request.setStatus(
                "OPEN"
        );

        request.setPriority(
                "MEDIUM"
        );

        request.setAssigneeId(
                null
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
                Optional.of(
                        existingTask
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

        assertNull(
                existingTask.getAssignee()
        );

        assertNull(
                result.getAssigneeId()
        );

        assertNull(
                result.getAssigneeName()
        );

        assertNull(
                result.getAssigneeEmail()
        );

        verify(
                userRepository,
                never()
        )
        .findById(
                any()
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

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                10L,
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        task
                )
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
    // ALL FILTERS
    // =========================================================

    @Test
    void getTasksByProjectPaged_allFiltersNormalizeToNull() {

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
                        "   ",
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
    // INVALID DUE DATE FILTER
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
                                                null,
                                                null,
                                                null,
                                                "NEXT_YEAR",
                                                PageRequest.of(
                                                        0,
                                                        10
                                                )
                                        )
                );

        assertEquals(
                "Invalid due date filter: NEXT_YEAR",
                exception.getMessage()
        );

        verify(
                taskRepository,
                never()
        )
        .searchTasksForOwnedProject(
                any(),
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
    // DUE TODAY ALIAS
    // =========================================================

    @Test
    void getTasksByProjectPaged_todayAlias_normalizesToDueToday() {

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
                                eq("DUE_TODAY"),
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
                        null,
                        null,
                        null,
                        "today",
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
                eq("DUE_TODAY"),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(pageable)
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

        return task;
    }

    /*
     * Sequence 13A test helper.
     */
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
}