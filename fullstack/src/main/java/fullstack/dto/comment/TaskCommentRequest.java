package fullstack.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaskCommentRequest {

    @NotBlank(message = "Comment text is required.")
    @Size(
        max = 2000,
        message = "Comment text cannot exceed 2000 characters."
    )
    private String commentText;

    public TaskCommentRequest() {
    }

    public TaskCommentRequest(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}