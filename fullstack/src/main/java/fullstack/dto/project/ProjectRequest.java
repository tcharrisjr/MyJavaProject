package fullstack.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequest {

    // =========================================================
    // NAME
    // =========================================================

    @NotBlank(
        message = "Project name is required."
    )
    @Size(
        max = 200,
        message = "Project name cannot exceed 200 characters."
    )
    private String name;

    // =========================================================
    // DESCRIPTION
    // =========================================================

    @Size(
        max = 2000,
        message = "Project description cannot exceed 2000 characters."
    )
    private String description;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectRequest() {
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name =
                name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description =
                description;
    }
}