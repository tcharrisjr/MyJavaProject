package fullstack.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import fullstack.dto.task.TaskRequest;

import fullstack.security.JwtAuthenticationFilter;

import fullstack.service.ProjectService;
import fullstack.service.TaskService;


/**
 * =============================================================
 * TASK CONTROLLER INTEGRATION TEST
 * =============================================================
 *
 * Tests the TaskController MVC layer independently from the
 * production JWT security implementation.
 *
 * Security behavior is tested separately by:
 *
 * SecurityIntegrationTest
 *
 * This class verifies:
 *
 * 1. Get project tasks
 * 2. Get task statistics
 * 3. Create task
 * 4. Update task
 * 5. Delete task
 * 6. Paginated task retrieval
 * 7. Negative-page validation
 * 8. Minimum page-size validation
 * 9. Maximum page-size validation
 * 10. Sort-field validation
 * 11. Sort-direction validation
 * 12. Task-request validation
 * 13. Authenticated username propagation
 *
 * =============================================================
 */
@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerIntegrationTest {


    // =========================================================
    // TEST CONSTANTS
    // =========================================================

    private static final String TEST_USER_EMAIL =
            "testuser@example.com";

    private static final Long PROJECT_ID =
            1L;

    private static final Long TASK_ID =
            10L;


    // =========================================================
    // MOCK MVC
    // =========================================================

    @Autowired
    private MockMvc mockMvc;


    // =========================================================
    // MOCK SERVICES
    // =========================================================

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private ProjectService projectService;


    // =========================================================
    // AUTHENTICATED PRINCIPAL HELPER
    // =========================================================

    private Principal authenticatedUser() {

        return () -> TEST_USER_EMAIL;
    }


    // =========================================================
    // TEST 1
    //
    // GET ALL TASKS FOR PROJECT
    //
    // GET /api/projects/{projectId}/tasks
    // =========================================================

    @Test
    void getTasks_authenticatedUser_returnsOk()
            throws Exception {

        when(
                taskService.getTasksByProject(
                        PROJECT_ID,
                        TEST_USER_EMAIL
                )
        )
        .thenReturn(
                Collections.emptyList()
        );


        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isOk()
        )

        .andExpect(
                content().json("[]")
        );


        verify(
                taskService
        )
        .getTasksByProject(
                PROJECT_ID,
                TEST_USER_EMAIL
        );
    }


    // =========================================================
    // TEST 2
    //
    // GET TASK STATISTICS
    //
    // GET /api/tasks/stats
    // =========================================================

    @Test
    void getTaskStats_authenticatedUser_returnsOk()
            throws Exception {

        Map<String, Long> stats =
                Map.of(
                        "projects", 2L,
                        "totalTasks", 12L,
                        "open", 5L,
                        "inProgress", 4L,
                        "completed", 3L
                );


        when(
                projectService.getProjectStats(
                        TEST_USER_EMAIL
                )
        )
        .thenReturn(
                stats
        );


        mockMvc.perform(

                get("/api/tasks/stats")

                        .principal(
                                authenticatedUser()
                        )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isOk()
        )

        .andExpect(
                content().json(
                        """
                        {
                          "projects": 2,
                          "totalTasks": 12,
                          "open": 5,
                          "inProgress": 4,
                          "completed": 3
                        }
                        """
                )
        );


        verify(
                projectService
        )
        .getProjectStats(
                TEST_USER_EMAIL
        );
    }


    // =========================================================
    // TEST 3
    //
    // CREATE TASK
    //
    // POST /api/projects/{projectId}/tasks
    //
    // Expected:
    //
    // HTTP 201 Created
    // =========================================================

    @Test
    void createTask_validRequest_returnsCreated()
            throws Exception {

        when(
                taskService.createTask(
                        eq(PROJECT_ID),
                        any(TaskRequest.class),
                        eq(TEST_USER_EMAIL)
                )
        )
        .thenReturn(
                null
        );


        mockMvc.perform(

                post(
                        "/api/projects/{projectId}/tasks",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .contentType(
                        MediaType.APPLICATION_JSON
                )

                .content(
                        """
                        {
                          "title": "Integration Test Task",
                          "description": "Task created by controller test",
                          "status": "OPEN",
                          "priority": "MEDIUM",
                          "dueDate": "2026-09-15"
                        }
                        """
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isCreated()
        );


        verify(
                taskService
        )
        .createTask(
                eq(PROJECT_ID),
                any(TaskRequest.class),
                eq(TEST_USER_EMAIL)
        );
    }


    // =========================================================
    // TEST 4
    //
    // UPDATE TASK
    //
    // PUT /api/projects/{projectId}/tasks/{taskId}
    //
    // Expected:
    //
    // HTTP 200 OK
    // =========================================================

    @Test
    void updateTask_validRequest_returnsOk()
            throws Exception {

        when(
                taskService.updateTask(
                        eq(PROJECT_ID),
                        eq(TASK_ID),
                        any(TaskRequest.class),
                        eq(TEST_USER_EMAIL)
                )
        )
        .thenReturn(
                null
        );


        mockMvc.perform(

                put(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        PROJECT_ID,
                        TASK_ID
                )

                .principal(
                        authenticatedUser()
                )

                .contentType(
                        MediaType.APPLICATION_JSON
                )

                .content(
                        """
                        {
                          "title": "Updated Integration Task",
                          "description": "Updated task description",
                          "status": "IN_PROGRESS",
                          "priority": "HIGH",
                          "dueDate": "2026-09-20"
                        }
                        """
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isOk()
        );


        verify(
                taskService
        )
        .updateTask(
                eq(PROJECT_ID),
                eq(TASK_ID),
                any(TaskRequest.class),
                eq(TEST_USER_EMAIL)
        );
    }


    // =========================================================
    // TEST 5
    //
    // DELETE TASK
    //
    // DELETE /api/projects/{projectId}/tasks/{taskId}
    //
    // Expected:
    //
    // HTTP 204 No Content
    // =========================================================

    @Test
    void deleteTask_authenticatedUser_returnsNoContent()
            throws Exception {

        mockMvc.perform(

                delete(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        PROJECT_ID,
                        TASK_ID
                )

                .principal(
                        authenticatedUser()
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isNoContent()
        );


        verify(
                taskService
        )
        .deleteTask(
                PROJECT_ID,
                TASK_ID,
                TEST_USER_EMAIL
        );
    }


    // =========================================================
    // TEST 6
    //
    // PAGINATED TASK LIST
    //
    // GET /api/projects/{projectId}/tasks/page
    // =========================================================

    @Test
    void getTasksPaged_validRequest_returnsOk()
            throws Exception {

        when(
                taskService.getTasksByProjectPaged(
                        eq(PROJECT_ID),
                        eq(TEST_USER_EMAIL),
                        eq("OPEN"),
                        eq("HIGH"),
                        eq("test"),
                        eq("overdue"),
                        any(Pageable.class)
                )
        )
        .thenReturn(
                Page.empty()
        );


        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks/page",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .param(
                        "page",
                        "0"
                )

                .param(
                        "size",
                        "10"
                )

                .param(
                        "status",
                        "OPEN"
                )

                .param(
                        "priority",
                        "HIGH"
                )

                .param(
                        "search",
                        "test"
                )

                .param(
                        "dueDateFilter",
                        "overdue"
                )

                .param(
                        "sortBy",
                        "dueDate"
                )

                .param(
                        "sortDirection",
                        "desc"
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isOk()
        );


        verify(
                taskService
        )
        .getTasksByProjectPaged(
                eq(PROJECT_ID),
                eq(TEST_USER_EMAIL),
                eq("OPEN"),
                eq("HIGH"),
                eq("test"),
                eq("overdue"),
                any(Pageable.class)
        );
    }


    // =========================================================
    // TEST 7
    //
    // NEGATIVE PAGE NUMBER
    //
    // Expected:
    //
    // HTTP 400 Bad Request
    // =========================================================

    @Test
    void getTasksPaged_negativePage_returnsBadRequest()
            throws Exception {

        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks/page",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .param(
                        "page",
                        "-1"
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isBadRequest()
        );
    }


    // =========================================================
    // TEST 8
    //
    // PAGE SIZE BELOW MINIMUM
    //
    // Expected:
    //
    // HTTP 400 Bad Request
    // =========================================================

    @Test
    void getTasksPaged_pageSizeZero_returnsBadRequest()
            throws Exception {

        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks/page",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .param(
                        "size",
                        "0"
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isBadRequest()
        );
    }


    // =========================================================
    // TEST 9
    //
    // PAGE SIZE ABOVE MAXIMUM
    //
    // Expected:
    //
    // HTTP 400 Bad Request
    // =========================================================

    @Test
    void getTasksPaged_pageSizeAboveMaximum_returnsBadRequest()
            throws Exception {

        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks/page",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .param(
                        "size",
                        "101"
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isBadRequest()
        );
    }


    // =========================================================
    // TEST 10
    //
    // INVALID SORT FIELD
    //
    // Expected:
    //
    // HTTP 400 Bad Request
    // =========================================================

    @Test
    void getTasksPaged_invalidSortField_returnsBadRequest()
            throws Exception {

        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks/page",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .param(
                        "sortBy",
                        "notARealField"
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isBadRequest()
        );
    }


    // =========================================================
    // TEST 11
    //
    // INVALID SORT DIRECTION
    //
    // Expected:
    //
    // HTTP 400 Bad Request
    // =========================================================

    @Test
    void getTasksPaged_invalidSortDirection_returnsBadRequest()
            throws Exception {

        mockMvc.perform(

                get(
                        "/api/projects/{projectId}/tasks/page",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .param(
                        "sortDirection",
                        "sideways"
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isBadRequest()
        );
    }


    // =========================================================
    // TEST 12
    //
    // TASK REQUEST VALIDATION
    //
    // Missing title should fail request validation.
    // =========================================================

    @Test
    void createTask_missingTitle_returnsBadRequest()
            throws Exception {

        mockMvc.perform(

                post(
                        "/api/projects/{projectId}/tasks",
                        PROJECT_ID
                )

                .principal(
                        authenticatedUser()
                )

                .contentType(
                        MediaType.APPLICATION_JSON
                )

                .content(
                        """
                        {
                          "description": "Missing title",
                          "status": "OPEN",
                          "priority": "MEDIUM"
                        }
                        """
                )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isBadRequest()
        );
    }
}