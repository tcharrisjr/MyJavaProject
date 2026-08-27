package fullstack.dto.task;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TaskRequest {

    // =========================================================
    // TITLE
    // =========================================================

    @NotBlank(
        message = "Task title is required."
    )
    @Size(
        max = 200,
        message = "Task title cannot exceed 200 characters."
    )
    private String title;

    // =========================================================
    // DESCRIPTION
    // =========================================================

    @Size(
        max = 2000,
        message = "Task description cannot exceed 2000 characters."
    )
    private String description;

    // =========================================================
    // STATUS
    // =========================================================

    @Pattern(
        regexp =
            "OPEN|IN_PROGRESS|COMPLETED",
        message =
            "Status must be OPEN, IN_PROGRESS, or COMPLETED."
    )
    private String status = "OPEN";

    // =========================================================
    // PRIORITY
    // =========================================================

    @Pattern(
        regexp =
            "LOW|MEDIUM|HIGH",
        message =
            "Priority must be LOW, MEDIUM, or HIGH."
    )
    private String priority = "MEDIUM";

    // =========================================================
    // DUE DATE
    // =========================================================

    private LocalDate dueDate;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskRequest() {
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title) {

        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(
            String priority) {

        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(
            LocalDate dueDate) {

        this.dueDate = dueDate;
    }
}