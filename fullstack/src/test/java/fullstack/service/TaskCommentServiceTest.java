package fullstack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import fullstack.dto.comment.TaskCommentRequest;
import fullstack.dto.comment.TaskCommentResponse;
import fullstack.model.ActivityType;
import fullstack.model.AppUser;
import fullstack.model.Project;
import fullstack.model.Task;
import fullstack.model.TaskComment;
import fullstack.repository.TaskCommentRepository;
import fullstack.repository.TaskRepository;
import fullstack.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final Long PROJECT_ID =
            1L;

    private static final Long TASK_ID =
            10L;

    private static final Long COMMENT_ID =
            100L;

    private static final String EMAIL =
            "test@example.com";

    // =========================================================
    // MOCKS
    // =========================================================

    @Mock
    private TaskCommentRepository
            taskCommentRepository;

    @Mock
    private TaskRepository
            taskRepository;

    @Mock
    private UserRepository
            userRepository;

    /*
     * Sequence 15A
     */
    @Mock
    private ProjectActivityService
            projectActivityService;

    // =========================================================
    // SERVICE
    // =========================================================

    private TaskCommentService
            taskCommentService;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        taskCommentService =
                new TaskCommentService(
                        taskCommentRepository,
                        taskRepository,
                        userRepository,
                        projectActivityService
                );
    }

    // =========================================================
    // GET COMMENTS
    // =========================================================

    @Test
    void getComments_whenTaskOwned_returnsComments() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        task,
                        user,
                        "First comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findByTaskIdOrderByCreatedAtAsc(
                                TASK_ID
                        )
        )
        .thenReturn(
                List.of(
                        comment
                )
        );

        List<TaskCommentResponse> result =
                taskCommentService
                        .getComments(
                                PROJECT_ID,
                                TASK_ID,
                                EMAIL
                        );

        assertNotNull(
                result
        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                COMMENT_ID,
                result.get(0).getId()
        );

        assertEquals(
                TASK_ID,
                result.get(0).getTaskId()
        );

        assertEquals(
                "First comment.",
                result.get(0).getCommentText()
        );

        assertEquals(
                "Test User",
                result.get(0).getAuthorName()
        );

        assertEquals(
                EMAIL,
                result.get(0).getAuthorEmail()
        );
    }

    // =========================================================
    // GET COMMENTS - TASK NOT OWNED
    // =========================================================

    @Test
    void getComments_whenTaskNotOwned_throws404() {

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                TASK_ID,
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .getComments(
                                                PROJECT_ID,
                                                TASK_ID,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Task not found.",
                exception.getReason()
        );

        verify(
                taskCommentRepository,
                never()
        )
        .findByTaskIdOrderByCreatedAtAsc(
                any()
        );
    }

    // =========================================================
    // CREATE COMMENT
    // =========================================================

    @Test
    void createComment_whenValid_createsComment() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskCommentRequest request =
                request(
                        "   Backend work is complete.   "
                );

        whenOwnedTask(
                task
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                taskCommentRepository.save(
                        any(TaskComment.class)
                )
        )
        .thenAnswer(
                invocation -> {

                    TaskComment comment =
                            invocation.getArgument(
                                    0
                            );

                    comment.setId(
                            COMMENT_ID
                    );

                    return comment;
                }
        );

        TaskCommentResponse result =
                taskCommentService
                        .createComment(
                                PROJECT_ID,
                                TASK_ID,
                                request,
                                EMAIL
                        );

        assertEquals(
                COMMENT_ID,
                result.getId()
        );

        assertEquals(
                TASK_ID,
                result.getTaskId()
        );

        assertEquals(
                5L,
                result.getUserId()
        );

        assertEquals(
                "Test User",
                result.getAuthorName()
        );

        assertEquals(
                EMAIL,
                result.getAuthorEmail()
        );

        assertEquals(
                "Backend work is complete.",
                result.getCommentText()
        );

        assertNotNull(
                result.getCreatedAt()
        );

        ArgumentCaptor<TaskComment> captor =
                ArgumentCaptor.forClass(
                        TaskComment.class
                );

        verify(
                taskCommentRepository
        )
        .save(
                captor.capture()
        );

        TaskComment saved =
                captor.getValue();

        assertSame(
                task,
                saved.getTask()
        );

        assertSame(
                user,
                saved.getUser()
        );

        assertEquals(
                "Backend work is complete.",
                saved.getCommentText()
        );

        assertNotNull(
                saved.getCreatedAt()
        );
    }

    // =========================================================
    // SEQUENCE 15A - COMMENT ADDED ACTIVITY
    // =========================================================

    @Test
    void createComment_recordsCommentAddedActivity() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskCommentRequest request =
                request(
                        "New comment."
                );

        whenOwnedTask(
                task
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                taskCommentRepository.save(
                        any(TaskComment.class)
                )
        )
        .thenAnswer(
                invocation -> {

                    TaskComment comment =
                            invocation.getArgument(
                                    0
                            );

                    comment.setId(
                            COMMENT_ID
                    );

                    return comment;
                }
        );

        taskCommentService
                .createComment(
                        PROJECT_ID,
                        TASK_ID,
                        request,
                        EMAIL
                );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.COMMENT_ADDED,
                "comment",
                null,
                "New comment.",
                "Added a comment to task \"Test Task\""
        );
    }

    // =========================================================
    // CREATE COMMENT - BLANK
    // =========================================================

    @Test
    void createComment_whenTextBlank_throws400() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskCommentRequest request =
                request(
                        "   "
                );

        whenOwnedTask(
                task
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        user
                )
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .createComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Comment text is required.",
                exception.getReason()
        );

        verify(
                taskCommentRepository,
                never()
        )
        .save(
                any(TaskComment.class)
        );
    }

    // =========================================================
    // CREATE COMMENT - TOO LONG
    // =========================================================

    @Test
    void createComment_whenTextTooLong_throws400() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskCommentRequest request =
                request(
                        "A".repeat(
                                2001
                        )
                );

        whenOwnedTask(
                task
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        user
                )
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .createComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                request,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Comment text cannot exceed 2000 characters.",
                exception.getReason()
        );
    }

    // =========================================================
    // CREATE COMMENT - AUTHENTICATED USER NOT FOUND
    // =========================================================

    @Test
    void createComment_whenUserNotFound_throws401() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        whenOwnedTask(
                task
        );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .createComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                request(
                                                        "Comment"
                                                ),
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        assertEquals(
                "Authenticated user not found.",
                exception.getReason()
        );

        verify(
                taskCommentRepository,
                never()
        )
        .save(
                any(TaskComment.class)
        );
    }

    // =========================================================
    // UPDATE COMMENT
    // =========================================================

    @Test
    void updateComment_whenValid_updatesComment() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        task,
                        user,
                        "Original comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        when(
                taskCommentRepository.save(
                        comment
                )
        )
        .thenReturn(
                comment
        );

        TaskCommentResponse result =
                taskCommentService
                        .updateComment(
                                PROJECT_ID,
                                TASK_ID,
                                COMMENT_ID,
                                request(
                                        "   Updated comment.   "
                                ),
                                EMAIL
                        );

        assertEquals(
                "Updated comment.",
                result.getCommentText()
        );

        assertNotNull(
                result.getUpdatedAt()
        );

        assertEquals(
                "Updated comment.",
                comment.getCommentText()
        );
    }

    // =========================================================
    // SEQUENCE 15A - COMMENT UPDATED ACTIVITY
    // =========================================================

    @Test
    void updateComment_whenTextChanges_recordsActivity() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        task,
                        user,
                        "Old comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        when(
                taskCommentRepository.save(
                        comment
                )
        )
        .thenReturn(
                comment
        );

        taskCommentService
                .updateComment(
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID,
                        request(
                                "New comment."
                        ),
                        EMAIL
                );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.COMMENT_UPDATED,
                "comment",
                "Old comment.",
                "New comment.",
                "Updated a comment on task \"Test Task\""
        );
    }

    // =========================================================
    // SEQUENCE 15A - UNCHANGED COMMENT
    // =========================================================

    @Test
    void updateComment_whenTextUnchanged_doesNotRecordActivity() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        task,
                        user,
                        "Same comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        when(
                taskCommentRepository.save(
                        comment
                )
        )
        .thenReturn(
                comment
        );

        taskCommentService
                .updateComment(
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID,
                        request(
                                "Same comment."
                        ),
                        EMAIL
                );

        verify(
                projectActivityService,
                never()
        )
        .recordTaskFieldChange(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // UPDATE COMMENT - COMMENT NOT FOUND
    // =========================================================

    @Test
    void updateComment_whenCommentNotFound_throws404() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.empty()
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .updateComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                COMMENT_ID,
                                                request(
                                                        "Updated"
                                                ),
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Comment not found.",
                exception.getReason()
        );
    }

    // =========================================================
    // UPDATE COMMENT - WRONG TASK
    // =========================================================

    @Test
    void updateComment_whenCommentBelongsToDifferentTask_throws404() {

        Project project =
                createProject();

        Task requestedTask =
                createTask(
                        project
                );

        Task otherTask =
                new Task();

        otherTask.setId(
                999L
        );

        otherTask.setProject(
                project
        );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        otherTask,
                        user,
                        "Comment"
                );

        whenOwnedTask(
                requestedTask
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .updateComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                COMMENT_ID,
                                                request(
                                                        "Updated"
                                                ),
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Comment not found.",
                exception.getReason()
        );
    }

    // =========================================================
    // UPDATE COMMENT - NOT AUTHOR
    // =========================================================

    @Test
    void updateComment_whenUserNotAuthor_throws403() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser otherUser =
                createUser(
                        8L,
                        "Other User",
                        "other@example.com"
                );

        TaskComment comment =
                createComment(
                        task,
                        otherUser,
                        "Other user's comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .updateComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                COMMENT_ID,
                                                request(
                                                        "Updated"
                                                ),
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        assertEquals(
                "You may only modify your own comments.",
                exception.getReason()
        );

        verify(
                taskCommentRepository,
                never()
        )
        .save(
                any(TaskComment.class)
        );
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================

    @Test
    void deleteComment_whenValid_deletesComment() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        task,
                        user,
                        "Delete me."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        taskCommentService
                .deleteComment(
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID,
                        EMAIL
                );

        verify(
                taskCommentRepository
        )
        .delete(
                comment
        );
    }

    // =========================================================
    // SEQUENCE 15A - COMMENT DELETED ACTIVITY
    // =========================================================

    @Test
    void deleteComment_whenValid_recordsActivity() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser user =
                createUser(
                        5L,
                        "Test User",
                        EMAIL
                );

        TaskComment comment =
                createComment(
                        task,
                        user,
                        "Deleted comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        taskCommentService
                .deleteComment(
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID,
                        EMAIL
                );

        verify(
                projectActivityService
        )
        .recordTaskFieldChange(
                project,
                task,
                EMAIL,
                ActivityType.COMMENT_DELETED,
                "comment",
                "Deleted comment.",
                null,
                "Deleted a comment from task \"Test Task\""
        );
    }

    // =========================================================
    // DELETE COMMENT - NOT AUTHOR
    // =========================================================

    @Test
    void deleteComment_whenUserNotAuthor_throws403() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        AppUser otherUser =
                createUser(
                        8L,
                        "Other User",
                        "other@example.com"
                );

        TaskComment comment =
                createComment(
                        task,
                        otherUser,
                        "Protected comment."
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .findById(
                                COMMENT_ID
                        )
        )
        .thenReturn(
                Optional.of(
                        comment
                )
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                taskCommentService
                                        .deleteComment(
                                                PROJECT_ID,
                                                TASK_ID,
                                                COMMENT_ID,
                                                EMAIL
                                        )
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(
                taskCommentRepository,
                never()
        )
        .delete(
                any(TaskComment.class)
        );

        verify(
                projectActivityService,
                never()
        )
        .recordTaskFieldChange(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // COMMENT COUNT
    // =========================================================

    @Test
    void getCommentCount_whenTaskOwned_returnsCount() {

        Project project =
                createProject();

        Task task =
                createTask(
                        project
                );

        whenOwnedTask(
                task
        );

        when(
                taskCommentRepository
                        .countByTaskId(
                                TASK_ID
                        )
        )
        .thenReturn(
                4L
        );

        long result =
                taskCommentService
                        .getCommentCount(
                                PROJECT_ID,
                                TASK_ID,
                                EMAIL
                        );

        assertEquals(
                4L,
                result
        );
    }

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private Project createProject() {

        Project project =
                new Project();

        project.setId(
                PROJECT_ID
        );

        project.setName(
                "Test Project"
        );

        project.setDescription(
                "Test project"
        );

        return project;
    }

    private Task createTask(
            Project project) {

        Task task =
                new Task();

        task.setId(
                TASK_ID
        );

        task.setProject(
                project
        );

        task.setTitle(
                "Test Task"
        );

        task.setDescription(
                "Test task"
        );

        task.setStatus(
                "OPEN"
        );

        task.setPriority(
                "MEDIUM"
        );

        return task;
    }

    private AppUser createUser(
            Long id,
            String name,
            String email) {

        AppUser user =
                new AppUser();

        user.setId(
                id
        );

        user.setName(
                name
        );

        user.setEmail(
                email
        );

        return user;
    }

    private TaskComment createComment(
            Task task,
            AppUser user,
            String commentText) {

        TaskComment comment =
                new TaskComment();

        comment.setId(
                COMMENT_ID
        );

        comment.setTask(
                task
        );

        comment.setUser(
                user
        );

        comment.setCommentText(
                commentText
        );

        comment.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        31,
                        10,
                        0
                )
        );

        return comment;
    }

    private TaskCommentRequest request(
            String commentText) {

        TaskCommentRequest request =
                new TaskCommentRequest();

        request.setCommentText(
                commentText
        );

        return request;
    }

    private void whenOwnedTask(
            Task task) {

        when(
                taskRepository
                        .findByIdAndProject_IdAndProject_Owner_EmailIgnoreCase(
                                TASK_ID,
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                Optional.of(
                        task
                )
        );
    }
}