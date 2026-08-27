package fullstack.dto.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {

    // =========================================================
    // RESPONSE FIELDS
    // =========================================================

    private Long id;

    private Long projectId;

    private String title;

    private String description;

    private String status;

    private String priority;

    private LocalDate dueDate;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TaskResponse() {
    }

    public TaskResponse(

            Long id,

            Long projectId,

            String title,

            String description,

            String status,

            String priority,

            LocalDate dueDate,

            LocalDateTime createdDate,

            LocalDateTime updatedDate) {

        this.id =
                id;

        this.projectId =
                projectId;

        this.title =
                title;

        this.description =
                description;

        this.status =
                status;

        this.priority =
                priority;

        this.dueDate =
                dueDate;

        this.createdDate =
                createdDate;

        this.updatedDate =
                updatedDate;
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(
            Long id) {

        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(
            Long projectId) {

        this.projectId =
                projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title) {

        this.title =
                title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description =
                description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status =
                status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(
            String priority) {

        this.priority =
                priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(
            LocalDate dueDate) {

        this.dueDate =
                dueDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(
            LocalDateTime createdDate) {

        this.createdDate =
                createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(
            LocalDateTime updatedDate) {

        this.updatedDate =
                updatedDate;
    }
}