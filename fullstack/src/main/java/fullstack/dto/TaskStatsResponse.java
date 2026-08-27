package fullstack.dto;

public class TaskStatsResponse {

    private long total;
    private long open;
    private long inProgress;
    private long completed;
    private long overdue;

    public TaskStatsResponse() {
    }

    public TaskStatsResponse(
            long total,
            long open,
            long inProgress,
            long completed,
            long overdue) {

        this.total = total;
        this.open = open;
        this.inProgress = inProgress;
        this.completed = completed;
        this.overdue = overdue;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getOverdue() {
        return overdue;
    }

    public void setOverdue(long overdue) {
        this.overdue = overdue;
    }
}