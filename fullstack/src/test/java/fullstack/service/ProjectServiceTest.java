package fullstack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

import fullstack.model.AppUser;
import fullstack.model.Project;

import fullstack.repository.ProjectRepository;
import fullstack.repository.TaskRepository;
import fullstack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    // =========================================================
    // MOCK REPOSITORIES
    // =========================================================

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    // =========================================================
    // SERVICE UNDER TEST
    // =========================================================

    private ProjectService projectService;

    // =========================================================
    // TEST CONSTANTS
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
                        userRepository
                );
    }

    // =========================================================
    // GET PROJECTS
    // =========================================================

    @Test
    void getProjectsForUser_returnsOwnedProjects() {

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
                1L,
                result.get(0).getId()
        );

        assertEquals(
                "Project One",
                result.get(0).getName()
        );

        assertEquals(
                "First project",
                result.get(0).getDescription()
        );

        assertEquals(
                2L,
                result.get(1).getId()
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
    }

    // =========================================================
    // GET SINGLE PROJECT - WRONG OWNER
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
    void createProject_normalizesFieldsAndAssignsAppUserOwner() {

        AppUser owner =
                new AppUser();

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "   New Project   "
        );

        request.setDescription(
                "   New project description   "
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
                            invocation
                                    .getArgument(
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
                "New project description",
                result.getDescription()
        );

        ArgumentCaptor<Project> projectCaptor =
                ArgumentCaptor.forClass(
                        Project.class
                );

        verify(
                projectRepository
        )
        .save(
                projectCaptor.capture()
        );

        Project savedProject =
                projectCaptor.getValue();

        assertEquals(
                "New Project",
                savedProject.getName()
        );

        assertEquals(
                "New project description",
                savedProject.getDescription()
        );

        assertSame(
                owner,
                savedProject.getOwner()
        );
    }

    // =========================================================
    // CREATE PROJECT - DESCRIPTION NULL
    // =========================================================

    @Test
    void createProject_whenDescriptionNull_storesEmptyString() {

        AppUser owner =
                new AppUser();

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "Project"
        );

        request.setDescription(
                null
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
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        ProjectResponse result =
                projectService
                        .createProject(
                                request,
                                EMAIL
                        );

        assertEquals(
                "",
                result.getDescription()
        );
    }

    // =========================================================
    // CREATE PROJECT - USER NOT FOUND
    // =========================================================

    @Test
    void createProject_whenAppUserNotFound_throws401() {

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
    }

    // =========================================================
    // CREATE PROJECT - BLANK NAME
    // =========================================================

    @Test
    void createProject_whenNameBlank_throwsIllegalArgumentException() {

        AppUser owner =
                new AppUser();

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
    void updateProject_updatesFieldsAndPreservesOwnership() {

        AppUser owner =
                new AppUser();

        Project existingProject =
                createProjectEntity(
                        1L,
                        "Old Project",
                        "Old description"
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
                        30
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
                "   Updated description   "
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
                "Updated description",
                result.getDescription()
        );

        assertEquals(
                1L,
                existingProject.getId()
        );

        assertSame(
                owner,
                existingProject.getOwner()
        );

        assertEquals(
                createdDate,
                existingProject.getCreatedDate()
        );

        verify(
                projectRepository
        )
        .save(
                existingProject
        );
    }

    // =========================================================
    // UPDATE PROJECT - NOT OWNED
    // =========================================================

    @Test
    void updateProject_whenNotOwned_throws404() {

        ProjectRequest request =
                new ProjectRequest();

        request.setName(
                "Updated"
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
                        "Delete test"
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

        assertThrows(
                ResponseStatusException.class,
                () ->
                        projectService
                                .deleteProject(
                                        1L,
                                        EMAIL
                                )
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
    // GLOBAL PROJECT STATISTICS
    // =========================================================

    @Test
    void getProjectStats_returnsExpectedCounts() {

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

        Map<String, Long> result =
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
    // STEP 8 PROJECT HEALTH
    // =========================================================

    @Test
    void getProjectHealth_returnsProjectSpecificHealthStatistics() {

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
                                anyLong(),
                                any(),
                                any(LocalDate.class)
                        )
        )
        .thenReturn(
                2L
        );

        when(
                taskRepository
                        .countDueSoonTasksForOwnedProject(
                                anyLong(),
                                any(),
                                any(LocalDate.class),
                                any(LocalDate.class)
                        )
        )
        .thenReturn(
                3L
        );

        ProjectHealthResponse result =
                projectService
                        .getProjectHealth(
                                1L,
                                EMAIL
                        );

        assertEquals(
                10L,
                result.getTotalTasks()
        );

        assertEquals(
                3L,
                result.getOpenTasks()
        );

        assertEquals(
                2L,
                result.getInProgressTasks()
        );

        assertEquals(
                5L,
                result.getCompletedTasks()
        );

        assertEquals(
                2L,
                result.getOverdueTasks()
        );

        assertEquals(
                3L,
                result.getDueSoonTasks()
        );

        assertEquals(
                50,
                result.getCompletionPercentage()
        );
    }

    // =========================================================
    // PROJECT HEALTH - NO TASKS
    // =========================================================

    @Test
    void getProjectHealth_whenNoTasks_returnsZeroCompletionPercentage() {

        Project project =
                createProjectEntity(
                        1L,
                        "Empty Project",
                        ""
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
                0L
        );

        ProjectHealthResponse result =
                projectService
                        .getProjectHealth(
                                1L,
                                EMAIL
                        );

        assertEquals(
                0,
                result.getCompletionPercentage()
        );

        assertEquals(
                0L,
                result.getTotalTasks()
        );
    }

    // =========================================================
    // TEST HELPER
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
}