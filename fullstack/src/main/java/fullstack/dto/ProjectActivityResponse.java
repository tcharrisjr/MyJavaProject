package fullstack.dto;

import fullstack.model.Task;

import java.util.List;

public class ProjectActivityResponse {

    private List<Task> recentlyUpdated;

    private List<Task> recentlyCompleted;

    public ProjectActivityResponse() {
    }

    public ProjectActivityResponse(
            List<Task> recentlyUpdated,
            List<Task> recentlyCompleted) {

        this.recentlyUpdated =
                recentlyUpdated;

        this.recentlyCompleted =
                recentlyCompleted;
    }

    public List<Task> getRecentlyUpdated() {
        return recentlyUpdated;
    }

    public void setRecentlyUpdated(
            List<Task> recentlyUpdated) {

        this.recentlyUpdated =
                recentlyUpdated;
    }

    public List<Task> getRecentlyCompleted() {
        return recentlyCompleted;
    }

    public void setRecentlyCompleted(
            List<Task> recentlyCompleted) {

        this.recentlyCompleted =
                recentlyCompleted;
    }
}