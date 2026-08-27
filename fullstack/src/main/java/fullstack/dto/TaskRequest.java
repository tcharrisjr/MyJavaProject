package fullstack.dto;

import fullstack.model.TaskPriority;
import fullstack.model.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TaskRequest {

    @NotBlank(
        message = "Task title is required."
    )
    @Size(
        max = 200,
        message = "Task title cannot exceed 200 characters."
    )
    private String title;

    @Size(
        max = 2000,
        message = "Task description cannot exceed 2000 characters."
    )
    private String description;

    @NotNull(
        message = "Task status is required."
    )
    private TaskStatus status;

    @NotNull(
        message = "Task priority is required."
    )
    private TaskPriority priority;

    private LocalDate dueDate;

    @NotNull(
        message = "Project ID is required."
    )
    private Long projectId;

    public TaskRequest() {
    }

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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(
            TaskStatus status) {

        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(
            TaskPriority priority) {

        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(
            LocalDate dueDate) {

        this.dueDate = dueDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(
            Long projectId) {

        this.projectId = projectId;
    }
}