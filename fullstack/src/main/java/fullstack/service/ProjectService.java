package fullstack.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository
            projectRepository;

    private final TaskRepository
            taskRepository;

    private final UserRepository
            userRepository;

    /*
     * Sequence 15A
     *
     * Central activity-history service used to record
     * project-level audit events.
     */
    private final ProjectActivityService
            projectActivityService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectActivityService projectActivityService) {

        this.projectRepository =
                projectRepository;

        this.taskRepository =
                taskRepository;

        this.userRepository =
                userRepository;

        this.projectActivityService =
                projectActivityService;
    }

    // =========================================================
    // GET PROJECTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProjectResponse>
            getProjectsForUser(
                    String email) {

        return projectRepository
                .findAllByOwner_EmailIgnoreCaseOrderByCreatedDateDesc(
                        email
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    // =========================================================
    // GET PROJECT
    // =========================================================

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(
            Long projectId,
            String email) {

        Project project =
                getOwnedProject(
                        projectId,
                        email
                );

        return toResponse(
                project
        );
    }

    // =========================================================
    // CREATE PROJECT
    // =========================================================

    public ProjectResponse createProject(
            ProjectRequest request,
            String email) {

        AppUser owner =
                userRepository
                        .findByEmailIgnoreCase(
                                email
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Authenticated user was not found."
                                        )
                        );

        Project project =
                new Project();

        project.setName(
                normalizeName(
                        request.getName()
                )
        );

        project.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );

        project.setOwner(
                owner
        );

        Project savedProject =
                projectRepository.save(
                        project
                );

        /*
         * Sequence 15A - Activity History
         *
         * Record creation only after the project has been
         * successfully persisted so the activity row can
         * reference the generated project ID.
         */
        projectActivityService.recordActivity(
                savedProject,
                email,
                ActivityType.PROJECT_CREATED,
                "Created project \""
                        + savedProject.getName()
                        + "\""
        );

        return toResponse(
                savedProject
        );
    }

    // =========================================================
    // UPDATE PROJECT
    // =========================================================

    public ProjectResponse updateProject(
            Long projectId,
            ProjectRequest request,
            String email) {

        Project existingProject =
                getOwnedProject(
                        projectId,
                        email
                );

        /*
         * Sequence 15A
         *
         * Capture the original values before changing the
         * managed entity. These values are used to create
         * meaningful audit records after the update succeeds.
         */
        String oldName =
                existingProject.getName();

        String oldDescription =
                existingProject.getDescription();

        existingProject.setName(
                normalizeName(
                        request.getName()
                )
        );

        existingProject.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );

        Project savedProject =
                projectRepository.save(
                        existingProject
                );

        /*
         * Record project-name changes.
         *
         * Objects.equals is deliberately used so the comparison
         * remains null-safe even if older database data contains
         * an unexpected null value.
         */
        if (!Objects.equals(
                oldName,
                savedProject.getName())) {

            projectActivityService
                    .recordActivity(
                            savedProject,
                            null,
                            email,
                            ActivityType.PROJECT_UPDATED,
                            "name",
                            oldName,
                            savedProject.getName(),
                            "Changed project name from \""
                                    + oldName
                                    + "\" to \""
                                    + savedProject.getName()
                                    + "\""
                    );
        }

        /*
         * Record project-description changes.
         */
        if (!Objects.equals(
                oldDescription,
                savedProject.getDescription())) {

            projectActivityService
                    .recordActivity(
                            savedProject,
                            null,
                            email,
                            ActivityType.PROJECT_UPDATED,
                            "description",
                            oldDescription,
                            savedProject.getDescription(),
                            "Updated project description"
                    );
        }

        return toResponse(
                savedProject
        );
    }

    // =========================================================
    // DELETE PROJECT
    // =========================================================

    public void deleteProject(
            Long projectId,
            String email) {

        Project project =
                getOwnedProject(
                        projectId,
                        email
                );

        /*
         * Sequence 15A
         *
         * PROJECT_DELETED is intentionally NOT recorded here.
         *
         * The current project_activity schema is project-scoped
         * and its project foreign key uses ON DELETE CASCADE.
         *
         * Recording an activity immediately before deleting the
         * project would therefore create an audit row that SQL
         * Server would immediately remove with the project.
         *
         * We will preserve the existing delete behavior until
         * deletion-history semantics are addressed separately.
         */
        projectRepository.delete(
                project
        );
    }

    // =========================================================
    // GLOBAL PROJECT/TASK STATISTICS
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Long>
            getProjectStats(
                    String email) {

        long projects =
                projectRepository
                        .countByOwner_EmailIgnoreCase(
                                email
                        );

        long totalTasks =
                taskRepository
                        .countTasksForUser(
                                email
                        );

        long openTasks =
                taskRepository
                        .countTasksForUserByStatus(
                                email,
                                "OPEN"
                        );

        long inProgressTasks =
                taskRepository
                        .countTasksForUserByStatus(
                                email,
                                "IN_PROGRESS"
                        );

        long completedTasks =
                taskRepository
                        .countTasksForUserByStatus(
                                email,
                                "COMPLETED"
                        );

        Map<String, Long> stats =
                new LinkedHashMap<>();

        stats.put(
                "projects",
                projects
        );

        stats.put(
                "totalTasks",
                totalTasks
        );

        stats.put(
                "open",
                openTasks
        );

        stats.put(
                "inProgress",
                inProgressTasks
        );

        stats.put(
                "completed",
                completedTasks
        );

        return stats;
    }

    // =========================================================
    // EXISTING GLOBAL HEALTH ENDPOINT
    //
    // Preserved for compatibility.
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Long>
            getProjectHealth(
                    String email) {

        return getProjectStats(
                email
        );
    }

    // =========================================================
    // STEP 8
    // PROJECT-SPECIFIC HEALTH
    // =========================================================

    @Transactional(readOnly = true)
    public ProjectHealthResponse
            getProjectHealth(
                    Long projectId,
                    String email) {

        /*
         * Verify ownership before calculating statistics.
         */
        getOwnedProject(
                projectId,
                email
        );

        LocalDate today =
                LocalDate.now();

        LocalDate dueSoonEnd =
                today.plusDays(7);

        long totalTasks =
                taskRepository
                        .countTasksForOwnedProject(
                                projectId,
                                email
                        );

        long openTasks =
                taskRepository
                        .countTasksForOwnedProjectByStatus(
                                projectId,
                                email,
                                "OPEN"
                        );

        long inProgressTasks =
                taskRepository
                        .countTasksForOwnedProjectByStatus(
                                projectId,
                                email,
                                "IN_PROGRESS"
                        );

        long completedTasks =
                taskRepository
                        .countTasksForOwnedProjectByStatus(
                                projectId,
                                email,
                                "COMPLETED"
                        );

        long overdueTasks =
                taskRepository
                        .countOverdueTasksForOwnedProject(
                                projectId,
                                email,
                                today
                        );

        long dueSoonTasks =
                taskRepository
                        .countDueSoonTasksForOwnedProject(
                                projectId,
                                email,
                                today,
                                dueSoonEnd
                        );

        int completionPercentage =
                totalTasks == 0
                        ? 0
                        : (int) Math.round(
                                (
                                    completedTasks
                                    * 100.0
                                )
                                / totalTasks
                        );

        return new ProjectHealthResponse(
                totalTasks,
                openTasks,
                inProgressTasks,
                completedTasks,
                overdueTasks,
                dueSoonTasks,
                completionPercentage
        );
    }

    // =========================================================
    // OWNED PROJECT
    // =========================================================

    private Project getOwnedProject(
            Long projectId,
            String email) {

        return projectRepository
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
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private ProjectResponse toResponse(
            Project project) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedDate()
        );
    }

    // =========================================================
    // NAME NORMALIZATION
    // =========================================================

    private String normalizeName(
            String name) {

        if (
            name == null
            || name.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Project name is required."
            );
        }

        return name.trim();
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
}