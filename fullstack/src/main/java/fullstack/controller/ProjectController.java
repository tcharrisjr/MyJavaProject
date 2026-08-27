package fullstack.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fullstack.dto.project.ProjectHealthResponse;
import fullstack.dto.project.ProjectRequest;
import fullstack.dto.project.ProjectResponse;

import fullstack.service.ProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService
            projectService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectController(
            ProjectService projectService) {

        this.projectService =
                projectService;
    }

    // =========================================================
    // GLOBAL PROJECT STATISTICS
    //
    // GET /api/projects/stats
    // =========================================================

    @GetMapping("/stats")
    public Map<String, Long>
            getProjectStats(

                    Authentication authentication) {

        return projectService
                .getProjectStats(
                        authentication.getName()
                );
    }

    // =========================================================
    // EXISTING GLOBAL HEALTH
    //
    // GET /api/projects/health
    //
    // Preserved for compatibility.
    // =========================================================

    @GetMapping("/health")
    public Map<String, Long>
            getProjectHealth(

                    Authentication authentication) {

        return projectService
                .getProjectHealth(
                        authentication.getName()
                );
    }

    // =========================================================
    // STEP 8
    // PROJECT-SPECIFIC HEALTH
    //
    // GET /api/projects/{id}/health
    // =========================================================

    @GetMapping("/{id}/health")
    public ProjectHealthResponse
            getProjectHealth(

                    @PathVariable
                    Long id,

                    Authentication authentication) {

        return projectService
                .getProjectHealth(

                        id,

                        authentication.getName()
                );
    }

    // =========================================================
    // GET PROJECTS
    // =========================================================

    @GetMapping
    public List<ProjectResponse>
            getProjects(

                    Authentication authentication) {

        return projectService
                .getProjectsForUser(
                        authentication.getName()
                );
    }

    // =========================================================
    // GET PROJECT
    // =========================================================

    @GetMapping("/{id}")
    public ProjectResponse
            getProjectById(

                    @PathVariable
                    Long id,

                    Authentication authentication) {

        return projectService
                .getProjectById(

                        id,

                        authentication.getName()
                );
    }

    // =========================================================
    // CREATE PROJECT
    // =========================================================

    @PostMapping
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public ProjectResponse
            createProject(

                    @Valid
                    @RequestBody
                    ProjectRequest request,

                    Authentication authentication) {

        return projectService
                .createProject(

                        request,

                        authentication.getName()
                );
    }

    // =========================================================
    // UPDATE PROJECT
    // =========================================================

    @PutMapping("/{id}")
    public ProjectResponse
            updateProject(

                    @PathVariable
                    Long id,

                    @Valid
                    @RequestBody
                    ProjectRequest request,

                    Authentication authentication) {

        return projectService
                .updateProject(

                        id,

                        request,

                        authentication.getName()
                );
    }

    // =========================================================
    // DELETE PROJECT
    // =========================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(
            HttpStatus.NO_CONTENT
    )
    public void deleteProject(

            @PathVariable
            Long id,

            Authentication authentication) {

        projectService
                .deleteProject(

                        id,

                        authentication.getName()
                );
    }
}