package fullstack.dto;

public class ProjectStatsResponse {

    private Long projectId;

    private long totalTasks;

    private long openTasks;

    private long inProgressTasks;

    private long completedTasks;

    private long overdueTasks;

    private double completionPercentage;

    public ProjectStatsResponse() {
    }

    public ProjectStatsResponse(
            Long projectId,
            long totalTasks,
            long openTasks,
            long inProgressTasks,
            long completedTasks,
            long overdueTasks,
            double completionPercentage) {

        this.projectId = projectId;
        this.totalTasks = totalTasks;
        this.openTasks = openTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.overdueTasks = overdueTasks;
        this.completionPercentage =
                completionPercentage;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(
            Long projectId) {

        this.projectId = projectId;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(
            long totalTasks) {

        this.totalTasks = totalTasks;
    }

    public long getOpenTasks() {
        return openTasks;
    }

    public void setOpenTasks(
            long openTasks) {

        this.openTasks = openTasks;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(
            long inProgressTasks) {

        this.inProgressTasks =
                inProgressTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(
            long completedTasks) {

        this.completedTasks =
                completedTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(
            long overdueTasks) {

        this.overdueTasks =
                overdueTasks;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(
            double completionPercentage) {

        this.completionPercentage =
                completionPercentage;
    }
}