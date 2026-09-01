package fullstack.dto.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TaskResponse {

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
    // SEQUENCE 13A - ASSIGNEE FIELDS
    // =========================================================

    private Long assigneeId;

    private String assigneeName;

    private String assigneeEmail;

    // =========================================================
    // SEQUENCE 13B - LABEL FIELDS
    //
    // Labels are returned to React as simple names.
    //
    // Example:
    // ["Frontend", "Bug", "Urgent"]
    // =========================================================

    private Set<String> labels = new HashSet<>();

    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================

    public TaskResponse() {
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

        this.projectId = projectId;
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

    public LocalDateTime getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(
            LocalDateTime createdDate) {

        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {

        return updatedDate;
    }

    public void setUpdatedDate(
            LocalDateTime updatedDate) {

        this.updatedDate = updatedDate;
    }

    // =========================================================
    // ASSIGNEE GETTERS / SETTERS
    // =========================================================

    public Long getAssigneeId() {

        return assigneeId;
    }

    public void setAssigneeId(
            Long assigneeId) {

        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {

        return assigneeName;
    }

    public void setAssigneeName(
            String assigneeName) {

        this.assigneeName = assigneeName;
    }

    public String getAssigneeEmail() {

        return assigneeEmail;
    }

    public void setAssigneeEmail(
            String assigneeEmail) {

        this.assigneeEmail = assigneeEmail;
    }

    // =========================================================
    // LABEL GETTERS / SETTERS
    // =========================================================

    public Set<String> getLabels() {

        return labels;
    }

    public void setLabels(
            Set<String> labels) {

        this.labels =
                labels != null
                        ? labels
                        : new HashSet<>();
    }
}