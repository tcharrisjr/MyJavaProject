package fullstack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.activity.ProjectActivityResponse;
import fullstack.model.ActivityType;
import fullstack.model.AppUser;
import fullstack.model.Project;
import fullstack.model.ProjectActivity;
import fullstack.model.Task;
import fullstack.repository.ProjectActivityRepository;
import fullstack.repository.ProjectRepository;
import fullstack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectActivityServiceTest {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final Long PROJECT_ID =
            1L;

    private static final Long TASK_ID =
            10L;

    private static final Long ACTIVITY_ID =
            100L;

    private static final Long USER_ID =
            5L;

    private static final String EMAIL =
            "test@example.com";

    // =========================================================
    // MOCKS
    // =========================================================

    @Mock
    private ProjectActivityRepository
            projectActivityRepository;

    @Mock
    private ProjectRepository
            projectRepository;

    @Mock
    private UserRepository
            userRepository;

    // =========================================================
    // SERVICE
    // =========================================================

    private ProjectActivityService
            projectActivityService;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        projectActivityService =
                new ProjectActivityService(
                        projectActivityRepository,
                        projectRepository,
                        userRepository
                );
    }

    // =========================================================
    // GET PROJECT ACTIVITY
    // =========================================================

    @Test
    void getProjectActivity_whenProjectOwned_returnsActivity() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser();

        ProjectActivity activity =
                createActivity(
                        project,
                        task,
                        user
                );

        whenOwnedProject(
                project
        );

        when(
                projectActivityRepository
                        .findByProjectIdOrderByCreatedAtDesc(
                                PROJECT_ID
                        )
        )
        .thenReturn(
                List.of(
                        activity
                )
        );

        List<ProjectActivityResponse> result =
                projectActivityService
                        .getProjectActivity(
                                PROJECT_ID,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                1,
                result.size()
        );

        ProjectActivityResponse response =
                result.get(0);

        assertEquals(
                ACTIVITY_ID,
                response.getId()
        );

        assertEquals(
                PROJECT_ID,
                response.getProjectId()
        );

        assertEquals(
                TASK_ID,
                response.getTaskId()
        );

        assertEquals(
                USER_ID,
                response.getUserId()
        );

        assertEquals(
                "Test User",
                response.getUserName()
        );

        assertEquals(
                EMAIL,
                response.getUserEmail()
        );

        assertEquals(
                ActivityType.TASK_STATUS_CHANGED,
                response.getActivityType()
        );

        assertEquals(
                "status",
                response.getFieldName()
        );

        assertEquals(
                "OPEN",
                response.getOldValue()
        );

        assertEquals(
                "COMPLETED",
                response.getNewValue()
        );

        assertEquals(
                "Changed task status from OPEN to COMPLETED.",
                response.getDescription()
        );

        assertNotNull(
                response.getCreatedAt()
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - EMPTY
    // =========================================================

    @Test
    void getProjectActivity_whenNoActivity_returnsEmptyList() {

        Project project =
                createProject();

        whenOwnedProject(
                project
        );

        when(
                projectActivityRepository
                        .findByProjectIdOrderByCreatedAtDesc(
                                PROJECT_ID
                        )
        )
        .thenReturn(
                List.of()
        );

        List<ProjectActivityResponse> result =
                projectActivityService
                        .getProjectActivity(
                                PROJECT_ID,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                0,
                result.size()
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - TASK NULL
    // =========================================================

    @Test
    void getProjectActivity_whenProjectLevelActivity_taskIdIsNull() {

        Project project =
                createProject();

        AppUser user =
                createUser();

        ProjectActivity activity =
                createActivity(
                        project,
                        null,
                        user
                );

        activity.setActivityType(
                ActivityType.PROJECT_UPDATED
        );

        activity.setDescription(
                "Updated project name."
        );

        whenOwnedProject(
                project
        );

        when(
                projectActivityRepository
                        .findByProjectIdOrderByCreatedAtDesc(
                                PROJECT_ID
                        )
        )
        .thenReturn(
                List.of(
                        activity
                )
        );

        ProjectActivityResponse result =
                projectActivityService
                        .getProjectActivity(
                                PROJECT_ID,
                                EMAIL
                        )
                        .get(0);

        assertNull(
                result.getTaskId()
        );

        assertEquals(
                ActivityType.PROJECT_UPDATED,
                result.getActivityType()
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - NOT OWNED
    // =========================================================

    @Test
    void getProjectActivity_whenProjectNotOwned_throws404() {

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                PROJECT_ID,
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
                                projectActivityService
                                        .getProjectActivity(
                                                PROJECT_ID,
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
                projectActivityRepository,
                never()
        )
        .findByProjectIdOrderByCreatedAtDesc(
                any()
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - MISSING AUTHENTICATION
    // =========================================================

    @Test
    void getProjectActivity_whenEmailBlank_throws401() {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                projectActivityService
                                        .getProjectActivity(
                                                PROJECT_ID,
                                                "   "
                                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        assertEquals(
                "Authentication is required.",
                exception.getReason()
        );

        verify(
                projectRepository,
                never()
        )
        .findByIdAndOwner_EmailIgnoreCase(
                any(),
                any()
        );
    }

    // =========================================================
    // ACTIVITY COUNT
    // =========================================================

    @Test
    void getProjectActivityCount_whenProjectOwned_returnsCount() {

        Project project =
                createProject();

        whenOwnedProject(
                project
        );

        when(
                projectActivityRepository
                        .countByProjectId(
                                PROJECT_ID
                        )
        )
        .thenReturn(
                7L
        );

        long result =
                projectActivityService
                        .getProjectActivityCount(
                                PROJECT_ID,
                                EMAIL
                        );

        assertEquals(
                7L,
                result
        );

        verify(
                projectActivityRepository
        )
        .countByProjectId(
                PROJECT_ID
        );
    }

    // =========================================================
    // ACTIVITY COUNT - NOT OWNED
    // =========================================================

    @Test
    void getProjectActivityCount_whenProjectNotOwned_throws404() {

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                PROJECT_ID,
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
                                projectActivityService
                                        .getProjectActivityCount(
                                                PROJECT_ID,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(
                projectActivityRepository,
                never()
        )
        .countByProjectId(
                any()
        );
    }

    // =========================================================
    // RECORD ACTIVITY
    // =========================================================

    @Test
    void recordActivity_whenValid_savesAndReturnsResponse() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser();

        whenAuthenticatedUser(
                user
        );

        when(
                projectActivityRepository.save(
                        any(ProjectActivity.class)
                )
        )
        .thenAnswer(
                invocation -> {

                    ProjectActivity activity =
                            invocation.getArgument(
                                    0
                            );

                    activity.setId(
                            ACTIVITY_ID
                    );

                    return activity;
                }
        );

        ProjectActivityResponse result =
                projectActivityService
                        .recordActivity(
                                project,
                                task,
                                EMAIL,
                                ActivityType.TASK_UPDATED,
                                "priority",
                                "LOW",
                                "HIGH",
                                "Changed task priority."
                        );

        assertEquals(
                ACTIVITY_ID,
                result.getId()
        );

        assertEquals(
                PROJECT_ID,
                result.getProjectId()
        );

        assertEquals(
                TASK_ID,
                result.getTaskId()
        );

        assertEquals(
                USER_ID,
                result.getUserId()
        );

        assertEquals(
                ActivityType.TASK_UPDATED,
                result.getActivityType()
        );

        assertEquals(
                "priority",
                result.getFieldName()
        );

        assertEquals(
                "LOW",
                result.getOldValue()
        );

        assertEquals(
                "HIGH",
                result.getNewValue()
        );

        assertEquals(
                "Changed task priority.",
                result.getDescription()
        );

        assertNotNull(
                result.getCreatedAt()
        );

        ArgumentCaptor<ProjectActivity> captor =
                ArgumentCaptor.forClass(
                        ProjectActivity.class
                );

        verify(
                projectActivityRepository
        )
        .save(
                captor.capture()
        );

        ProjectActivity savedActivity =
                captor.getValue();

        assertSame(
                project,
                savedActivity.getProject()
        );

        assertSame(
                task,
                savedActivity.getTask()
        );

        assertSame(
                user,
                savedActivity.getUser()
        );

        assertEquals(
                ActivityType.TASK_UPDATED,
                savedActivity.getActivityType()
        );
    }

    // =========================================================
    // RECORD ACTIVITY - NORMALIZATION
    // =========================================================

    @Test
    void recordActivity_normalizesOptionalValuesAndDescription() {

        Project project =
                createProject();

        AppUser user =
                createUser();

        whenAuthenticatedUser(
                user
        );

        when(
                projectActivityRepository.save(
                        any(ProjectActivity.class)
                )
        )
        .thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        projectActivityService
                .recordActivity(
                        project,
                        null,
                        EMAIL,
                        ActivityType.PROJECT_UPDATED,
                        "   ",
                        "   ",
                        " New Name ",
                        "   Updated project name.   "
                );

        ArgumentCaptor<ProjectActivity> captor =
                ArgumentCaptor.forClass(
                        ProjectActivity.class
                );

        verify(
                projectActivityRepository
        )
        .save(
                captor.capture()
        );

        ProjectActivity saved =
                captor.getValue();

        assertNull(
                saved.getFieldName()
        );

        assertNull(
                saved.getOldValue()
        );

        assertEquals(
                "New Name",
                saved.getNewValue()
        );

        assertEquals(
                "Updated project name.",
                saved.getDescription()
        );
    }

    // =========================================================
    // RECORD ACTIVITY - PROJECT REQUIRED
    // =========================================================

    @Test
    void recordActivity_whenProjectNull_throwsException() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                projectActivityService
                                        .recordActivity(
                                                null,
                                                null,
                                                EMAIL,
                                                ActivityType.TASK_UPDATED,
                                                null,
                                                null,
                                                null,
                                                "Description"
                                        )
                );

        assertEquals(
                "Project is required.",
                exception.getMessage()
        );

        verify(
                projectActivityRepository,
                never()
        )
        .save(
                any(ProjectActivity.class)
        );
    }

    // =========================================================
    // RECORD ACTIVITY - TYPE REQUIRED
    // =========================================================

    @Test
    void recordActivity_whenActivityTypeNull_throwsException() {

        Project project =
                createProject();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                projectActivityService
                                        .recordActivity(
                                                project,
                                                null,
                                                EMAIL,
                                                null,
                                                null,
                                                null,
                                                null,
                                                "Description"
                                        )
                );

        assertEquals(
                "Activity type is required.",
                exception.getMessage()
        );
    }

    // =========================================================
    // RECORD ACTIVITY - DESCRIPTION REQUIRED
    // =========================================================

    @Test
    void recordActivity_whenDescriptionBlank_throwsException() {

        Project project =
                createProject();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                projectActivityService
                                        .recordActivity(
                                                project,
                                                null,
                                                EMAIL,
                                                ActivityType.PROJECT_UPDATED,
                                                null,
                                                null,
                                                null,
                                                "   "
                                        )
                );

        assertEquals(
                "Activity description is required.",
                exception.getMessage()
        );
    }

    // =========================================================
    // RECORD ACTIVITY - USER NOT FOUND
    // =========================================================

    @Test
    void recordActivity_whenUserNotFound_throws401() {

        Project project =
                createProject();

        when(
                userRepository
                        .findByEmailIgnoreCase(
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
                                projectActivityService
                                        .recordActivity(
                                                project,
                                                null,
                                                EMAIL,
                                                ActivityType.PROJECT_CREATED,
                                                null,
                                                null,
                                                null,
                                                "Created project."
                                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        assertEquals(
                "Authenticated user was not found.",
                exception.getReason()
        );

        verify(
                projectActivityRepository,
                never()
        )
        .save(
                any(ProjectActivity.class)
        );
    }

    // =========================================================
    // RECORD PROJECT ACTIVITY CONVENIENCE METHOD
    // =========================================================

    @Test
    void recordActivity_projectConvenienceMethod_recordsProjectActivity() {

        Project project =
                createProject();

        AppUser user =
                createUser();

        whenAuthenticatedUser(
                user
        );

        when(
                projectActivityRepository.save(
                        any(ProjectActivity.class)
                )
        )
        .thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        ProjectActivityResponse result =
                projectActivityService
                        .recordActivity(
                                project,
                                EMAIL,
                                ActivityType.PROJECT_CREATED,
                                "Created project."
                        );

        assertEquals(
                PROJECT_ID,
                result.getProjectId()
        );

        assertNull(
                result.getTaskId()
        );

        assertEquals(
                ActivityType.PROJECT_CREATED,
                result.getActivityType()
        );
    }

    // =========================================================
    // RECORD TASK ACTIVITY CONVENIENCE METHOD
    // =========================================================

    @Test
    void recordTaskActivity_recordsTaskActivity() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser();

        whenAuthenticatedUser(
                user
        );

        when(
                projectActivityRepository.save(
                        any(ProjectActivity.class)
                )
        )
        .thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        ProjectActivityResponse result =
                projectActivityService
                        .recordTaskActivity(
                                project,
                                task,
                                EMAIL,
                                ActivityType.TASK_CREATED,
                                "Created task."
                        );

        assertEquals(
                PROJECT_ID,
                result.getProjectId()
        );

        assertEquals(
                TASK_ID,
                result.getTaskId()
        );

        assertEquals(
                ActivityType.TASK_CREATED,
                result.getActivityType()
        );
    }

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private Project createProject() {

        Project project =
                new Project();

        project.setId(
                PROJECT_ID
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
            Project project) {

        Task task =
                new Task();

        task.setId(
                TASK_ID
        );

        task.setProject(
                project
        );

        task.setTitle(
                "Test Task"
        );

        task.setStatus(
                "OPEN"
        );

        task.setPriority(
                "MEDIUM"
        );

        return task;
    }

    private AppUser createUser() {

        AppUser user =
                new AppUser();

        user.setId(
                USER_ID
        );

        user.setName(
                "Test User"
        );

        user.setEmail(
                EMAIL
        );

        return user;
    }

    private ProjectActivity createActivity(
            Project project,
            Task task,
            AppUser user) {

        ProjectActivity activity =
                new ProjectActivity();

        activity.setId(
                ACTIVITY_ID
        );

        activity.setProject(
                project
        );

        activity.setTask(
                task
        );

        activity.setUser(
                user
        );

        activity.setActivityType(
                ActivityType.TASK_STATUS_CHANGED
        );

        activity.setFieldName(
                "status"
        );

        activity.setOldValue(
                "OPEN"
        );

        activity.setNewValue(
                "COMPLETED"
        );

        activity.setDescription(
                "Changed task status from OPEN to COMPLETED."
        );

        activity.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        31,
                        14,
                        0
                )
        );

        return activity;
    }

    private void whenOwnedProject(
            Project project) {

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        project
                )
        );
    }

    private void whenAuthenticatedUser(
            AppUser user) {

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        user
                )
        );
    }
}