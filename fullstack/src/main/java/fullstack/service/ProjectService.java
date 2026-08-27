package fullstack.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.project.ProjectHealthResponse;
import fullstack.dto.project.ProjectRequest;
import fullstack.dto.project.ProjectResponse;

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

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectService(

            ProjectRepository projectRepository,

            TaskRepository taskRepository,

            UserRepository userRepository) {

        this.projectRepository =
                projectRepository;

        this.taskRepository =
                taskRepository;

        this.userRepository =
                userRepository;
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