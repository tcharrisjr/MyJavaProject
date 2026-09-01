package fullstack.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fullstack.dto.comment.TaskCommentRequest;
import fullstack.dto.comment.TaskCommentResponse;
import fullstack.security.JwtService;
import fullstack.service.TaskCommentService;
import fullstack.service.UserService;

@WebMvcTest(TaskCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskCommentControllerIntegrationTest {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String EMAIL =
            "test@example.com";

    private static final Long PROJECT_ID =
            1L;

    private static final Long TASK_ID =
            10L;

    private static final Long COMMENT_ID =
            100L;

    // =========================================================
    // MVC
    // =========================================================

    @Autowired
    private MockMvc mockMvc;

    // =========================================================
    // MOCKED SERVICES
    // =========================================================

    @MockitoBean
    private TaskCommentService taskCommentService;

    /*
     * JwtAuthenticationFilter is discovered while the
     * WebMvcTest application context is created.
     *
     * MockMvc filters are disabled for these controller tests,
     * but Spring must still satisfy the filter constructor
     * dependencies while creating the test context.
     */
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    // =========================================================
    // GET COMMENTS
    // =========================================================

    @Test
    void getComments_whenAuthenticated_returnsOk()
            throws Exception {

        TaskCommentResponse comment =
                createCommentResponse();

        when(
                taskCommentService.getComments(
                        PROJECT_ID,
                        TASK_ID,
                        EMAIL
                )
        )
        .thenReturn(
                List.of(
                        comment
                )
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/tasks/{taskId}/comments",
                        PROJECT_ID,
                        TASK_ID
                )
                .principal(
                        principal()
                )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$[0].id")
                        .value(
                                COMMENT_ID
                        )
        )
        .andExpect(
                jsonPath("$[0].taskId")
                        .value(
                                TASK_ID
                        )
        )
        .andExpect(
                jsonPath("$[0].userId")
                        .value(
                                5L
                        )
        )
        .andExpect(
                jsonPath("$[0].authorName")
                        .value(
                                "Test User"
                        )
        )
        .andExpect(
                jsonPath("$[0].authorEmail")
                        .value(
                                EMAIL
                        )
        )
        .andExpect(
                jsonPath("$[0].commentText")
                        .value(
                                "Backend work is complete."
                        )
        );

        verify(
                taskCommentService
        )
        .getComments(
                PROJECT_ID,
                TASK_ID,
                EMAIL
        );
    }

    // =========================================================
    // GET COMMENTS - EMPTY
    // =========================================================

    @Test
    void getComments_whenNoComments_returnsEmptyArray()
            throws Exception {

        when(
                taskCommentService.getComments(
                        PROJECT_ID,
                        TASK_ID,
                        EMAIL
                )
        )
        .thenReturn(
                List.of()
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/tasks/{taskId}/comments",
                        PROJECT_ID,
                        TASK_ID
                )
                .principal(
                        principal()
                )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                content().json(
                        "[]"
                )
        );

        verify(
                taskCommentService
        )
        .getComments(
                PROJECT_ID,
                TASK_ID,
                EMAIL
        );
    }

    // =========================================================
    // CREATE COMMENT
    // =========================================================

    @Test
    void createComment_whenValid_returns201()
            throws Exception {

        TaskCommentResponse response =
                createCommentResponse();

        when(
                taskCommentService.createComment(
                        eq(
                                PROJECT_ID
                        ),
                        eq(
                                TASK_ID
                        ),
                        any(
                                TaskCommentRequest.class
                        ),
                        eq(
                                EMAIL
                        )
                )
        )
        .thenReturn(
                response
        );

        String requestJson =
                """
                {
                  "commentText": "Backend work is complete."
                }
                """;

        mockMvc.perform(
                post(
                        "/api/projects/{projectId}/tasks/{taskId}/comments",
                        PROJECT_ID,
                        TASK_ID
                )
                .principal(
                        principal()
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(
                        requestJson
                )
        )
        .andExpect(
                status().isCreated()
        )
        .andExpect(
                jsonPath("$.id")
                        .value(
                                COMMENT_ID
                        )
        )
        .andExpect(
                jsonPath("$.taskId")
                        .value(
                                TASK_ID
                        )
        )
        .andExpect(
                jsonPath("$.userId")
                        .value(
                                5L
                        )
        )
        .andExpect(
                jsonPath("$.authorName")
                        .value(
                                "Test User"
                        )
        )
        .andExpect(
                jsonPath("$.authorEmail")
                        .value(
                                EMAIL
                        )
        )
        .andExpect(
                jsonPath("$.commentText")
                        .value(
                                "Backend work is complete."
                        )
        );

        verify(
                taskCommentService
        )
        .createComment(
                eq(
                        PROJECT_ID
                ),
                eq(
                        TASK_ID
                ),
                any(
                        TaskCommentRequest.class
                ),
                eq(
                        EMAIL
                )
        );
    }

    // =========================================================
    // CREATE COMMENT - BLANK
    // =========================================================

    @Test
    void createComment_whenBlank_returns400()
            throws Exception {

        String requestJson =
                """
                {
                  "commentText": "   "
                }
                """;

        mockMvc.perform(
                post(
                        "/api/projects/{projectId}/tasks/{taskId}/comments",
                        PROJECT_ID,
                        TASK_ID
                )
                .principal(
                        principal()
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(
                        requestJson
                )
        )
        .andExpect(
                status().isBadRequest()
        );

        verify(
                taskCommentService,
                never()
        )
        .createComment(
                any(),
                any(),
                any(
                        TaskCommentRequest.class
                ),
                any()
        );
    }

    // =========================================================
    // CREATE COMMENT - TOO LONG
    // =========================================================

    @Test
    void createComment_whenTextExceeds2000Characters_returns400()
            throws Exception {

        String longComment =
                "A".repeat(
                        2001
                );

        String requestJson =
                """
                {
                  "commentText": "%s"
                }
                """.formatted(
                        longComment
                );

        mockMvc.perform(
                post(
                        "/api/projects/{projectId}/tasks/{taskId}/comments",
                        PROJECT_ID,
                        TASK_ID
                )
                .principal(
                        principal()
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(
                        requestJson
                )
        )
        .andExpect(
                status().isBadRequest()
        );

        verify(
                taskCommentService,
                never()
        )
        .createComment(
                any(),
                any(),
                any(
                        TaskCommentRequest.class
                ),
                any()
        );
    }

    // =========================================================
    // UPDATE COMMENT
    // =========================================================

    @Test
    void updateComment_whenValid_returns200()
            throws Exception {

        TaskCommentResponse response =
                createCommentResponse();

        response.setCommentText(
                "Updated comment."
        );

        response.setUpdatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        31,
                        11,
                        0
                )
        );

        when(
                taskCommentService.updateComment(
                        eq(
                                PROJECT_ID
                        ),
                        eq(
                                TASK_ID
                        ),
                        eq(
                                COMMENT_ID
                        ),
                        any(
                                TaskCommentRequest.class
                        ),
                        eq(
                                EMAIL
                        )
                )
        )
        .thenReturn(
                response
        );

        String requestJson =
                """
                {
                  "commentText": "Updated comment."
                }
                """;

        mockMvc.perform(
                put(
                        "/api/projects/{projectId}/tasks/{taskId}/comments/{commentId}",
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID
                )
                .principal(
                        principal()
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(
                        requestJson
                )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.id")
                        .value(
                                COMMENT_ID
                        )
        )
        .andExpect(
                jsonPath("$.taskId")
                        .value(
                                TASK_ID
                        )
        )
        .andExpect(
                jsonPath("$.commentText")
                        .value(
                                "Updated comment."
                        )
        )
        .andExpect(
                jsonPath("$.updatedAt")
                        .exists()
        );

        verify(
                taskCommentService
        )
        .updateComment(
                eq(
                        PROJECT_ID
                ),
                eq(
                        TASK_ID
                ),
                eq(
                        COMMENT_ID
                ),
                any(
                        TaskCommentRequest.class
                ),
                eq(
                        EMAIL
                )
        );
    }

    // =========================================================
    // UPDATE COMMENT - BLANK
    // =========================================================

    @Test
    void updateComment_whenBlank_returns400()
            throws Exception {

        String requestJson =
                """
                {
                  "commentText": ""
                }
                """;

        mockMvc.perform(
                put(
                        "/api/projects/{projectId}/tasks/{taskId}/comments/{commentId}",
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID
                )
                .principal(
                        principal()
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(
                        requestJson
                )
        )
        .andExpect(
                status().isBadRequest()
        );

        verify(
                taskCommentService,
                never()
        )
        .updateComment(
                any(),
                any(),
                any(),
                any(
                        TaskCommentRequest.class
                ),
                any()
        );
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================

    @Test
    void deleteComment_whenValid_returns204()
            throws Exception {

        mockMvc.perform(
                delete(
                        "/api/projects/{projectId}/tasks/{taskId}/comments/{commentId}",
                        PROJECT_ID,
                        TASK_ID,
                        COMMENT_ID
                )
                .principal(
                        principal()
                )
        )
        .andExpect(
                status().isNoContent()
        )
        .andExpect(
                content().string(
                        ""
                )
        );

        verify(
                taskCommentService
        )
        .deleteComment(
                PROJECT_ID,
                TASK_ID,
                COMMENT_ID,
                EMAIL
        );
    }

    // =========================================================
    // COMMENT COUNT
    // =========================================================

    @Test
    void getCommentCount_whenAuthenticated_returnsCount()
            throws Exception {

        when(
                taskCommentService.getCommentCount(
                        PROJECT_ID,
                        TASK_ID,
                        EMAIL
                )
        )
        .thenReturn(
                4L
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/tasks/{taskId}/comments/count",
                        PROJECT_ID,
                        TASK_ID
                )
                .principal(
                        principal()
                )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                content().string(
                        "4"
                )
        );

        verify(
                taskCommentService
        )
        .getCommentCount(
                PROJECT_ID,
                TASK_ID,
                EMAIL
        );
    }

    // =========================================================
    // PRINCIPAL
    // =========================================================

    private Principal principal() {

        return () ->
                EMAIL;
    }

    // =========================================================
    // TEST RESPONSE DATA
    // =========================================================

    private TaskCommentResponse createCommentResponse() {

        TaskCommentResponse response =
                new TaskCommentResponse();

        response.setId(
                COMMENT_ID
        );

        response.setTaskId(
                TASK_ID
        );

        response.setUserId(
                5L
        );

        response.setAuthorName(
                "Test User"
        );

        response.setAuthorEmail(
                EMAIL
        );

        response.setCommentText(
                "Backend work is complete."
        );

        response.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        31,
                        10,
                        0
                )
        );

        response.setUpdatedAt(
                null
        );

        return response;
    }
}