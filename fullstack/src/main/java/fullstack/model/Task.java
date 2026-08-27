package fullstack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
        nullable = false,
        length = 200
    )
    private String title;

    @Column(
        length = 2000
    )
    private String description;

    @Column(
        nullable = false,
        length = 30
    )
    private String status = "OPEN";

    @Column(
        nullable = false,
        length = 30
    )
    private String priority = "MEDIUM";

    @Column(
        name = "due_date"
    )
    private LocalDate dueDate;

    // =========================================================
    // CREATED DATE
    // =========================================================

    @Column(
        name = "created_date",
        nullable = false
    )
    private LocalDateTime createdDate;

    // =========================================================
    // UPDATED DATE
    // =========================================================

    @Column(
        name = "updated_date",
        nullable = false
    )
    private LocalDateTime updatedDate;

    // =========================================================
    // PROJECT RELATIONSHIP
    // =========================================================

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "project_id",
        nullable = false
    )
    @JsonIgnore
    private Project project;

    public Task() {
    }

    // =========================================================
    // PRE-PERSIST
    // =========================================================

    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdDate == null) {
            createdDate = now;
        }

        if (updatedDate == null) {
            updatedDate = now;
        }

        if (
            status == null
            || status.isBlank()
        ) {
            status = "OPEN";
        }

        if (
            priority == null
            || priority.isBlank()
        ) {
            priority = "MEDIUM";
        }
    }

    // =========================================================
    // PRE-UPDATE
    // =========================================================

    @PreUpdate
    public void preUpdate() {

        updatedDate =
                LocalDateTime.now();
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

    public Project getProject() {
        return project;
    }

    public void setProject(
            Project project) {

        this.project = project;
    }

    @JsonProperty("projectId")
    public Long getProjectId() {

        if (project == null) {
            return null;
        }

        return project.getId();
    }
}