package fullstack.dto;

public class ProjectHealthResponse {

    private Long projectId;

    private long dueSoonTasks;

    private long overdueTasks;

    private double completionPercentage;

    private double overduePercentage;

    private long recentlyCompletedTasks;

    private String healthStatus;

    public ProjectHealthResponse() {
    }

    public ProjectHealthResponse(
            Long projectId,
            long dueSoonTasks,
            long overdueTasks,
            double completionPercentage,
            double overduePercentage,
            long recentlyCompletedTasks,
            String healthStatus) {

        this.projectId = projectId;
        this.dueSoonTasks = dueSoonTasks;
        this.overdueTasks = overdueTasks;
        this.completionPercentage = completionPercentage;
        this.overduePercentage = overduePercentage;
        this.recentlyCompletedTasks = recentlyCompletedTasks;
        this.healthStatus = healthStatus;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public long getDueSoonTasks() {
        return dueSoonTasks;
    }

    public void setDueSoonTasks(long dueSoonTasks) {
        this.dueSoonTasks = dueSoonTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(
            double completionPercentage) {

        this.completionPercentage =
                completionPercentage;
    }

    public double getOverduePercentage() {
        return overduePercentage;
    }

    public void setOverduePercentage(
            double overduePercentage) {

        this.overduePercentage =
                overduePercentage;
    }

    public long getRecentlyCompletedTasks() {
        return recentlyCompletedTasks;
    }

    public void setRecentlyCompletedTasks(
            long recentlyCompletedTasks) {

        this.recentlyCompletedTasks =
                recentlyCompletedTasks;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(
            String healthStatus) {

        this.healthStatus =
                healthStatus;
    }
}