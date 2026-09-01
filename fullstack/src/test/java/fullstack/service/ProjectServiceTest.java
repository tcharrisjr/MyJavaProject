package fullstack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.project.ProjectHealthResponse;
import fullstack.dto.project.ProjectRequest;
import fullstack.dto.project.ProjectResponse;

import fullstack.model.ActivityType;
import fullstack.model.AppUser;
import fullstack.model.Project;

import fullstack.repository.ProjectRepository;
import fullstack.repository.TaskRepository;
import fullstack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    // =========================================================
    // MOCKS
    // =========================================================

    @Mock
    private ProjectRepository
            projectRepository;

    @Mock
    private TaskRepository
            taskRepository;

    @Mock
    private UserRepository
            userRepository;

    /*
     * Sequence 15A
     *
     * ProjectService now records audit/history events through
     * ProjectActivityService.
     */
    @Mock
    private ProjectActivityService
            projectActivityService;

    // =========================================================
    // SERVICE UNDER TEST
    // =========================================================

    private ProjectService
            projectService;

    // =========================================================
    // COMMON TEST DATA
    // =========================================================

    private static final String EMAIL =
            "test@example.com";

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        projectService =
                new ProjectService(
                        projectRepository,
                        taskRepository,
                        userRepository,
                        projectActivityService
                );
    }

    // =========================================================
    // GET PROJECTS
    // =========================================================

    @Test
    void getProjectsForUser_returnsOnlyProjectsOwnedByUser() {

        Project projectOne =
                createProjectEntity(
                        1L,
                        "Project One",
                        "First project"
                );

        Project projectTwo =
                createProjectEntity(
                        2L,
                        "Project Two",
                        "Second project"
                );

        when(
                projectRepository
                        .findAllByOwner_EmailIgnoreCaseOrderByCreatedDateDesc(
                                EMAIL
                        )
        )
        .thenReturn(
                List.of(
                        projectOne,
                        projectTwo
                )
        );

        List<ProjectResponse> result =
                projectService
                        .getProjectsForUser(
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Project One",
                result.get(0).getName()
        );

        assertEquals(
                "Project Two",
                result.get(1).getName()
        );

        verify(
                projectRepository
        )
        .findAllByOwner_EmailIgnoreCaseOrderByCreatedDateDesc(
                EMAIL
        );
    }

    // =========================================================
    // GET SINGLE PROJECT
    // =========================================================

    @Test
    void getProjectById_whenOwned_returnsProject() {

        Project project =
                createProjectEntity(
                        1L,
                        "Project One",
                        "Description"
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

        ProjectResponse result =
                projectService
                        .getProjectById(
                                1L,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "Project One",
                result.getName()
        );

        assertEquals(
                "Description",
                result.getDescription()
        );
    }

    // =========================================================
    // GET SINGLE PROJECT - NOT OWNED
    // =========================================================

    @Test
    void getProjectById_whenNotOwned_throws404() {

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                99L,
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
                                projectService
                                        .getProjectById(
                                                99L,
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
    }

    // =========================================================
    // CREATE PROJECT
    // =========================================================

    @Test
    void createProject_normalizesFieldsAssignsOwnerAndRecordsActivity() {

        AppUser owner =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "   New Project   "
        );

        request.setDescription(
                "   Project description   "
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        owner
                )
        );

        when(
                projectRepository.save(
                        any(Project.class)
                )
        )
        .thenAnswer(
                invocation -> {

                    Project project =
                            invocation.getArgument(
                                    0
                            );

                    project.setId(
                            10L
                    );

                    project.setCreatedDate(
                            LocalDateTime.now()
                    );

                    return project;
                }
        );

        ProjectResponse result =
                projectService
                        .createProject(
                                request,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                10L,
                result.getId()
        );

        assertEquals(
                "New Project",
                result.getName()
        );

        assertEquals(
                "Project description",
                result.getDescription()
        );

        ArgumentCaptor<Project> captor =
                ArgumentCaptor.forClass(
                        Project.class
                );

        verify(
                projectRepository
        )
        .save(
                captor.capture()
        );

        Project savedProject =
                captor.getValue();

        assertEquals(
                "New Project",
                savedProject.getName()
        );

        assertEquals(
                "Project description",
                savedProject.getDescription()
        );

        assertSame(
                owner,
                savedProject.getOwner()
        );

        /*
         * Sequence 15A
         *
         * A successfully created project must generate
         * PROJECT_CREATED history.
         */
        verify(
                projectActivityService
        )
        .recordActivity(
                savedProject,
                EMAIL,
                ActivityType.PROJECT_CREATED,
                "Created project \"New Project\""
        );
    }

    // =========================================================
    // CREATE PROJECT - AUTHENTICATED USER NOT FOUND
    // =========================================================

    @Test
    void createProject_whenAuthenticatedUserNotFound_throws401() {

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "Test Project"
        );

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
                                projectService
                                        .createProject(
                                                request,
                                                EMAIL
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
                projectRepository,
                never()
        )
        .save(
                any(Project.class)
        );

        verify(
                projectActivityService,
                never()
        )
        .recordActivity(
                any(Project.class),
                any(String.class),
                any(ActivityType.class),
                any(String.class)
        );
    }

    // =========================================================
    // CREATE PROJECT - BLANK NAME
    // =========================================================

    @Test
    void createProject_whenNameBlank_throwsIllegalArgumentException() {

        AppUser owner =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "   "
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        owner
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                projectService
                                        .createProject(
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                "Project name is required.",
                exception.getMessage()
        );

        verify(
                projectRepository,
                never()
        )
        .save(
                any(Project.class)
        );
    }

    // =========================================================
    // UPDATE PROJECT
    // =========================================================

    @Test
    void updateProject_updatesFieldsPreservesOwnershipAndRecordsActivity() {

        AppUser owner =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        Project existingProject =
                createProjectEntity(
                        1L,
                        "Old Name",
                        "Old Description"
                );

        existingProject.setOwner(
                owner
        );

        LocalDateTime createdDate =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        10,
                        0
                );

        existingProject.setCreatedDate(
                createdDate
        );

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "   Updated Project   "
        );

        request.setDescription(
                "   Updated Description   "
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
                        existingProject
                )
        );

        when(
                projectRepository.save(
                        existingProject
                )
        )
        .thenReturn(
                existingProject
        );

        ProjectResponse result =
                projectService
                        .updateProject(
                                1L,
                                request,
                                EMAIL
                        );

        assertEquals(
                "Updated Project",
                result.getName()
        );

        assertEquals(
                "Updated Description",
                result.getDescription()
        );

        assertSame(
                owner,
                existingProject.getOwner()
        );

        assertEquals(
                createdDate,
                existingProject.getCreatedDate()
        );

        assertEquals(
                1L,
                existingProject.getId()
        );

        verify(
                projectRepository
        )
        .save(
                existingProject
        );

        /*
         * Sequence 15A
         *
         * Name change produces one field-specific activity row.
         */
        verify(
                projectActivityService
        )
        .recordActivity(
                existingProject,
                null,
                EMAIL,
                ActivityType.PROJECT_UPDATED,
                "name",
                "Old Name",
                "Updated Project",
                "Changed project name from \"Old Name\" to \"Updated Project\""
        );

        /*
         * Description change produces another activity row.
         */
        verify(
                projectActivityService
        )
        .recordActivity(
                existingProject,
                null,
                EMAIL,
                ActivityType.PROJECT_UPDATED,
                "description",
                "Old Description",
                "Updated Description",
                "Updated project description"
        );
    }

    // =========================================================
    // UPDATE PROJECT - NO ACTUAL FIELD CHANGES
    // =========================================================

    @Test
    void updateProject_whenValuesUnchanged_doesNotRecordActivity() {

        Project existingProject =
                createProjectEntity(
                        1L,
                        "Same Project",
                        "Same Description"
                );

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "Same Project"
        );

        request.setDescription(
                "Same Description"
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
                        existingProject
                )
        );

        when(
                projectRepository.save(
                        existingProject
                )
        )
        .thenReturn(
                existingProject
        );

        ProjectResponse result =
                projectService
                        .updateProject(
                                1L,
                                request,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                "Same Project",
                result.getName()
        );

        verify(
                projectActivityService,
                never()
        )
        .recordActivity(
                any(Project.class),
                any(),
                any(String.class),
                any(ActivityType.class),
                any(),
                any(),
                any(),
                any(String.class)
        );
    }

    // =========================================================
    // UPDATE PROJECT - NOT OWNED
    // =========================================================

    @Test
    void updateProject_whenNotOwned_throws404AndDoesNotSave() {

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "Updated Project"
        );

        request.setDescription(
                "Updated Description"
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
                                projectService
                                        .updateProject(
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
                projectRepository,
                never()
        )
        .save(
                any(Project.class)
        );
    }

    // =========================================================
    // DELETE PROJECT
    // =========================================================

    @Test
    void deleteProject_whenOwned_deletesProject() {

        Project project =
                createProjectEntity(
                        1L,
                        "Delete Me",
                        "Description"
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

        projectService
                .deleteProject(
                        1L,
                        EMAIL
                );

        verify(
                projectRepository
        )
        .delete(
                project
        );

        /*
         * Current Sequence 15A design intentionally does not
         * record PROJECT_DELETED because project_activity
         * currently cascades when a project is deleted.
         */
        verify(
                projectActivityService,
                never()
        )
        .recordActivity(
                any(Project.class),
                any(String.class),
                any(ActivityType.class),
                any(String.class)
        );
    }

    // =========================================================
    // DELETE PROJECT - NOT OWNED
    // =========================================================

    @Test
    void deleteProject_whenNotOwned_throws404AndDoesNotDelete() {

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
                                projectService
                                        .deleteProject(
                                                1L,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(
                projectRepository,
                never()
        )
        .delete(
                any(Project.class)
        );
    }

    // =========================================================
    // PROJECT STATISTICS
    // =========================================================

    @Test
    void getProjectStats_returnsRepositoryCounts() {

        when(
                projectRepository
                        .countByOwner_EmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                3L
        );

        when(
                taskRepository
                        .countTasksForUser(
                                EMAIL
                        )
        )
        .thenReturn(
                12L
        );

        when(
                taskRepository
                        .countTasksForUserByStatus(
                                EMAIL,
                                "OPEN"
                        )
        )
        .thenReturn(
                5L
        );

        when(
                taskRepository
                        .countTasksForUserByStatus(
                                EMAIL,
                                "IN_PROGRESS"
                        )
        )
        .thenReturn(
                4L
        );

        when(
                taskRepository
                        .countTasksForUserByStatus(
                                EMAIL,
                                "COMPLETED"
                        )
        )
        .thenReturn(
                3L
        );

        var result =
                projectService
                        .getProjectStats(
                                EMAIL
                        );

        assertEquals(
                3L,
                result.get(
                        "projects"
                )
        );

        assertEquals(
                12L,
                result.get(
                        "totalTasks"
                )
        );

        assertEquals(
                5L,
                result.get(
                        "open"
                )
        );

        assertEquals(
                4L,
                result.get(
                        "inProgress"
                )
        );

        assertEquals(
                3L,
                result.get(
                        "completed"
                )
        );
    }

    // =========================================================
    // GLOBAL PROJECT HEALTH
    // =========================================================

    @Test
    void getProjectHealth_withoutProjectId_returnsGlobalStats() {

        when(
                projectRepository
                        .countByOwner_EmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                2L
        );

        when(
                taskRepository
                        .countTasksForUser(
                                EMAIL
                        )
        )
        .thenReturn(
                8L
        );

        when(
                taskRepository
                        .countTasksForUserByStatus(
                                EMAIL,
                                "OPEN"
                        )
        )
        .thenReturn(
                3L
        );

        when(
                taskRepository
                        .countTasksForUserByStatus(
                                EMAIL,
                                "IN_PROGRESS"
                        )
        )
        .thenReturn(
                2L
        );

        when(
                taskRepository
                        .countTasksForUserByStatus(
                                EMAIL,
                                "COMPLETED"
                        )
        )
        .thenReturn(
                3L
        );

        var result =
                projectService
                        .getProjectHealth(
                                EMAIL
                        );

        assertEquals(
                2L,
                result.get(
                        "projects"
                )
        );

        assertEquals(
                8L,
                result.get(
                        "totalTasks"
                )
        );
    }

    // =========================================================
    // PROJECT-SPECIFIC HEALTH
    // =========================================================

    @Test
    void getProjectHealth_whenProjectOwned_returnsHealthResponse() {

        Project project =
                createProjectEntity(
                        1L,
                        "Health Project",
                        "Health test"
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
                        .countTasksForOwnedProject(
                                1L,
                                EMAIL
                        )
        )
        .thenReturn(
                10L
        );

        when(
                taskRepository
                        .countTasksForOwnedProjectByStatus(
                                1L,
                                EMAIL,
                                "OPEN"
                        )
        )
        .thenReturn(
                3L
        );

        when(
                taskRepository
                        .countTasksForOwnedProjectByStatus(
                                1L,
                                EMAIL,
                                "IN_PROGRESS"
                        )
        )
        .thenReturn(
                2L
        );

        when(
                taskRepository
                        .countTasksForOwnedProjectByStatus(
                                1L,
                                EMAIL,
                                "COMPLETED"
                        )
        )
        .thenReturn(
                5L
        );

        when(
                taskRepository
                        .countOverdueTasksForOwnedProject(
                                1L,
                                EMAIL,
                                LocalDate.now()
                        )
        )
        .thenReturn(
                1L
        );

        when(
                taskRepository
                        .countDueSoonTasksForOwnedProject(
                                1L,
                                EMAIL,
                                LocalDate.now(),
                                LocalDate.now()
                                        .plusDays(7)
                        )
        )
        .thenReturn(
                2L
        );

        ProjectHealthResponse result =
                projectService
                        .getProjectHealth(
                                1L,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        verify(
                taskRepository
        )
        .countTasksForOwnedProject(
                1L,
                EMAIL
        );

        verify(
                taskRepository
        )
        .countTasksForOwnedProjectByStatus(
                1L,
                EMAIL,
                "OPEN"
        );

        verify(
                taskRepository
        )
        .countTasksForOwnedProjectByStatus(
                1L,
                EMAIL,
                "IN_PROGRESS"
        );

        verify(
                taskRepository
        )
        .countTasksForOwnedProjectByStatus(
                1L,
                EMAIL,
                "COMPLETED"
        );
    }

    // =========================================================
    // PROJECT-SPECIFIC HEALTH - NOT OWNED
    // =========================================================

    @Test
    void getProjectHealth_whenProjectNotOwned_throws404() {

        when(
                projectRepository
                        .findByIdAndOwner_EmailIgnoreCase(
                                99L,
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
                                projectService
                                        .getProjectHealth(
                                                99L,
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
        .countTasksForOwnedProject(
                99L,
                EMAIL
        );
    }

    // =========================================================
    // TEST HELPER - PROJECT
    // =========================================================

    private Project createProjectEntity(
            Long id,
            String name,
            String description) {

        Project project =
                new Project();

        project.setId(
                id
        );

        project.setName(
                name
        );

        project.setDescription(
                description
        );

        project.setCreatedDate(
                LocalDateTime.now()
        );

        return project;
    }

    // =========================================================
    // TEST HELPER - USER
    // =========================================================

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