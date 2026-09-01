package fullstack.dto.activity;

import java.time.LocalDateTime;

import fullstack.model.ActivityType;

public class ProjectActivityResponse {

    private Long id;

    private Long projectId;

    private Long taskId;

    private Long userId;

    private String userName;

    private String userEmail;

    private ActivityType activityType;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private String description;

    private LocalDateTime createdAt;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ProjectActivityResponse() {
    }

    // =========================================================
    // ID
    // =========================================================

    public Long getId() {

        return id;
    }

    public void setId(
            Long id) {

        this.id =
                id;
    }

    // =========================================================
    // PROJECT ID
    // =========================================================

    public Long getProjectId() {

        return projectId;
    }

    public void setProjectId(
            Long projectId) {

        this.projectId =
                projectId;
    }

    // =========================================================
    // TASK ID
    // =========================================================

    public Long getTaskId() {

        return taskId;
    }

    public void setTaskId(
            Long taskId) {

        this.taskId =
                taskId;
    }

    // =========================================================
    // USER ID
    // =========================================================

    public Long getUserId() {

        return userId;
    }

    public void setUserId(
            Long userId) {

        this.userId =
                userId;
    }

    // =========================================================
    // USER NAME
    // =========================================================

    public String getUserName() {

        return userName;
    }

    public void setUserName(
            String userName) {

        this.userName =
                userName;
    }

    // =========================================================
    // USER EMAIL
    // =========================================================

    public String getUserEmail() {

        return userEmail;
    }

    public void setUserEmail(
            String userEmail) {

        this.userEmail =
                userEmail;
    }

    // =========================================================
    // ACTIVITY TYPE
    // =========================================================

    public ActivityType getActivityType() {

        return activityType;
    }

    public void setActivityType(
            ActivityType activityType) {

        this.activityType =
                activityType;
    }

    // =========================================================
    // FIELD NAME
    // =========================================================

    public String getFieldName() {

        return fieldName;
    }

    public void setFieldName(
            String fieldName) {

        this.fieldName =
                fieldName;
    }

    // =========================================================
    // OLD VALUE
    // =========================================================

    public String getOldValue() {

        return oldValue;
    }

    public void setOldValue(
            String oldValue) {

        this.oldValue =
                oldValue;
    }

    // =========================================================
    // NEW VALUE
    // =========================================================

    public String getNewValue() {

        return newValue;
    }

    public void setNewValue(
            String newValue) {

        this.newValue =
                newValue;
    }

    // =========================================================
    // DESCRIPTION
    // =========================================================

    public String getDescription() {

        return description;
    }

    public void setDescription(
            String description) {

        this.description =
                description;
    }

    // =========================================================
    // CREATED AT
    // =========================================================

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt =
                createdAt;
    }
}