package fullstack.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.activity.ProjectActivityResponse;
import fullstack.service.ProjectActivityService;

@RestController
@RequestMapping(
        "/api/projects/{projectId}/activity"
)
public class ProjectActivityController {

    private final ProjectActivityService
            projectActivityService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectActivityController(
            ProjectActivityService projectActivityService) {

        this.projectActivityService =
                projectActivityService;
    }

    // =========================================================
    // GET PROJECT ACTIVITY
    //
    // GET /api/projects/{projectId}/activity
    // =========================================================

    @GetMapping
    public List<ProjectActivityResponse>
            getProjectActivity(
                    @PathVariable
                    Long projectId,
                    Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        return projectActivityService
                .getProjectActivity(
                        projectId,
                        email
                );
    }

    // =========================================================
    // GET PROJECT ACTIVITY COUNT
    //
    // GET /api/projects/{projectId}/activity/count
    // =========================================================

    @GetMapping("/count")
    public long getProjectActivityCount(
            @PathVariable
            Long projectId,
            Principal principal) {

        String email =
                getAuthenticatedEmail(
                        principal
                );

        return projectActivityService
                .getProjectActivityCount(
                        projectId,
                        email
                );
    }

    // =========================================================
    // AUTHENTICATED EMAIL
    // =========================================================

    private String getAuthenticatedEmail(
            Principal principal) {

        if (
            principal == null
            || principal.getName() == null
            || principal.getName().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
            );
        }

        return principal
                .getName()
                .trim();
    }
}