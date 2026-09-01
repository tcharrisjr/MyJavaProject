package fullstack.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_comments")
public class TaskComment {

    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // TASK
    // =========================================================
    //
    // Every comment belongs to exactly one task.
    //
    // The database column is:
    //
    // task_comments.task_id
    //
    // FetchType.LAZY prevents Hibernate from automatically
    // loading the entire Task object unless it is needed.
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            nullable = false
    )
    private Task task;

    // =========================================================
    // COMMENT AUTHOR
    // =========================================================
    //
    // Sequence 13C
    //
    // AppUser is the application's authenticated user entity.
    //
    // The database column is:
    //
    // task_comments.user_id
    //
    // This relationship lets us determine who created each
    // task comment.
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private AppUser user;

    // =========================================================
    // COMMENT TEXT
    // =========================================================

    @Column(
            name = "comment_text",
            nullable = false,
            length = 2000
    )
    private String commentText;

    // =========================================================
    // CREATED DATE
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;
    
 // =========================================================
 // UPDATED DATE
 // =========================================================
 //
 // Remains null until the comment is edited.
 // =========================================================

 @Column(
         name = "updated_at"
 )
 private LocalDateTime updatedAt;

    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================
    //
    // Required by JPA / Hibernate.
    // =========================================================

    public TaskComment() {
    }

    // =========================================================
    // CONVENIENCE CONSTRUCTOR
    // =========================================================

    public TaskComment(
            Task task,
            AppUser user,
            String commentText,
            LocalDateTime createdAt) {

        this.task =
                task;

        this.user =
                user;

        this.commentText =
                commentText;

        this.createdAt =
                createdAt;
    }

    // =========================================================
    // PRE-PERSIST
    // =========================================================
    //
    // Automatically populate createdAt when a new comment
    // is inserted if the caller did not explicitly provide
    // a timestamp.
    // =========================================================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now();
        }
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
    // TASK
    // =========================================================

    public Task getTask() {

        return task;
    }

    public void setTask(
            Task task) {

        this.task =
                task;
    }

    // =========================================================
    // USER / COMMENT AUTHOR
    // =========================================================

    public AppUser getUser() {

        return user;
    }

    public void setUser(
            AppUser user) {

        this.user =
                user;
    }

    // =========================================================
    // COMMENT TEXT
    // =========================================================

    public String getCommentText() {

        return commentText;
    }

    public void setCommentText(
            String commentText) {

        this.commentText =
                commentText;
    }

    // =========================================================
    // CREATED DATE
    // =========================================================

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt =
                createdAt;
    }
 // =========================================================
 // UPDATED DATE
 // =========================================================

 public LocalDateTime getUpdatedAt() {

     return updatedAt;
 }

 public void setUpdatedAt(
         LocalDateTime updatedAt) {

     this.updatedAt =
             updatedAt;
 }
}