package fullstack.dto.project;

public class ProjectHealthResponse {

    private long totalTasks;

    private long openTasks;

    private long inProgressTasks;

    private long completedTasks;

    private long overdueTasks;

    private long dueSoonTasks;

    private int completionPercentage;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public ProjectHealthResponse() {
    }

    public ProjectHealthResponse(

            long totalTasks,

            long openTasks,

            long inProgressTasks,

            long completedTasks,

            long overdueTasks,

            long dueSoonTasks,

            int completionPercentage) {

        this.totalTasks =
                totalTasks;

        this.openTasks =
                openTasks;

        this.inProgressTasks =
                inProgressTasks;

        this.completedTasks =
                completedTasks;

        this.overdueTasks =
                overdueTasks;

        this.dueSoonTasks =
                dueSoonTasks;

        this.completionPercentage =
                completionPercentage;
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(
            long totalTasks) {

        this.totalTasks =
                totalTasks;
    }

    public long getOpenTasks() {
        return openTasks;
    }

    public void setOpenTasks(
            long openTasks) {

        this.openTasks =
                openTasks;
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

    public long getDueSoonTasks() {
        return dueSoonTasks;
    }

    public void setDueSoonTasks(
            long dueSoonTasks) {

        this.dueSoonTasks =
                dueSoonTasks;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(
            int completionPercentage) {

        this.completionPercentage =
                completionPercentage;
    }
}