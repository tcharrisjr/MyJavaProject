package fullstack.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fullstack.dto.activity.ProjectActivityResponse;
import fullstack.model.ActivityType;
import fullstack.security.JwtService;
import fullstack.service.ProjectActivityService;
import fullstack.service.UserService;

@WebMvcTest(ProjectActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectActivityControllerIntegrationTest {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String EMAIL =
            "test@example.com";

    private static final Long PROJECT_ID =
            1L;

    private static final Long TASK_ID =
            10L;

    private static final Long ACTIVITY_ID =
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
    private ProjectActivityService
            projectActivityService;

    /*
     * JwtAuthenticationFilter is discovered during
     * WebMvcTest application-context startup.
     *
     * Even though servlet filters are disabled for these
     * controller tests, Spring still creates the filter bean
     * and must satisfy all constructor dependencies.
     */
    @MockitoBean
    private JwtService jwtService;

    /*
     * JwtAuthenticationFilter also depends on UserService.
     *
     * WebMvcTest does not load regular service beans unless
     * they are explicitly included, so this dependency must
     * be supplied as a mock.
     */
    @MockitoBean
    private UserService userService;

    // =========================================================
    // GET PROJECT ACTIVITY
    // =========================================================

    @Test
    void getProjectActivity_whenAuthenticated_returns200()
            throws Exception {

        ProjectActivityResponse activity =
                createActivityResponse();

        when(
                projectActivityService
                        .getProjectActivity(
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                List.of(
                        activity
                )
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity",
                        PROJECT_ID
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
                                ACTIVITY_ID
                        )
        )
        .andExpect(
                jsonPath("$[0].projectId")
                        .value(
                                PROJECT_ID
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
                jsonPath("$[0].userName")
                        .value(
                                "Test User"
                        )
        )
        .andExpect(
                jsonPath("$[0].userEmail")
                        .value(
                                EMAIL
                        )
        )
        .andExpect(
                jsonPath("$[0].activityType")
                        .value(
                                "TASK_STATUS_CHANGED"
                        )
        )
        .andExpect(
                jsonPath("$[0].fieldName")
                        .value(
                                "status"
                        )
        )
        .andExpect(
                jsonPath("$[0].oldValue")
                        .value(
                                "OPEN"
                        )
        )
        .andExpect(
                jsonPath("$[0].newValue")
                        .value(
                                "COMPLETED"
                        )
        )
        .andExpect(
                jsonPath("$[0].description")
                        .value(
                                "Changed task status from OPEN to COMPLETED."
                        )
        )
        .andExpect(
                jsonPath("$[0].createdAt")
                        .exists()
        );

        verify(
                projectActivityService
        )
        .getProjectActivity(
                PROJECT_ID,
                EMAIL
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - EMPTY
    // =========================================================

    @Test
    void getProjectActivity_whenNoActivity_returnsEmptyArray()
            throws Exception {

        when(
                projectActivityService
                        .getProjectActivity(
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                List.of()
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity",
                        PROJECT_ID
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
                projectActivityService
        )
        .getProjectActivity(
                PROJECT_ID,
                EMAIL
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - PROJECT LEVEL
    // =========================================================

    @Test
    void getProjectActivity_whenTaskIdNull_returnsNullTaskId()
            throws Exception {

        ProjectActivityResponse activity =
                createActivityResponse();

        activity.setTaskId(
                null
        );

        activity.setActivityType(
                ActivityType.PROJECT_UPDATED
        );

        activity.setFieldName(
                "name"
        );

        activity.setOldValue(
                "Old Project"
        );

        activity.setNewValue(
                "New Project"
        );

        activity.setDescription(
                "Changed project name."
        );

        when(
                projectActivityService
                        .getProjectActivity(
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                List.of(
                        activity
                )
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity",
                        PROJECT_ID
                )
                .principal(
                        principal()
                )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$[0].projectId")
                        .value(
                                PROJECT_ID
                        )
        )
        .andExpect(
                jsonPath("$[0].taskId")
                        .doesNotExist()
        )
        .andExpect(
                jsonPath("$[0].activityType")
                        .value(
                                "PROJECT_UPDATED"
                        )
        )
        .andExpect(
                jsonPath("$[0].fieldName")
                        .value(
                                "name"
                        )
        );
    }

    // =========================================================
    // ACTIVITY COUNT
    // =========================================================

    @Test
    void getProjectActivityCount_whenAuthenticated_returns200()
            throws Exception {

        when(
                projectActivityService
                        .getProjectActivityCount(
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                7L
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity/count",
                        PROJECT_ID
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
                        "7"
                )
        );

        verify(
                projectActivityService
        )
        .getProjectActivityCount(
                PROJECT_ID,
                EMAIL
        );
    }

    // =========================================================
    // ACTIVITY COUNT - ZERO
    // =========================================================

    @Test
    void getProjectActivityCount_whenNoActivity_returnsZero()
            throws Exception {

        when(
                projectActivityService
                        .getProjectActivityCount(
                                PROJECT_ID,
                                EMAIL
                        )
        )
        .thenReturn(
                0L
        );

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity/count",
                        PROJECT_ID
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
                        "0"
                )
        );

        verify(
                projectActivityService
        )
        .getProjectActivityCount(
                PROJECT_ID,
                EMAIL
        );
    }

    // =========================================================
    // GET PROJECT ACTIVITY - MISSING PRINCIPAL
    // =========================================================

    @Test
    void getProjectActivity_whenPrincipalMissing_returns401()
            throws Exception {

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity",
                        PROJECT_ID
                )
        )
        .andExpect(
                status().isUnauthorized()
        );

        verify(
                projectActivityService,
                never()
        )
        .getProjectActivity(
                PROJECT_ID,
                EMAIL
        );
    }

    // =========================================================
    // ACTIVITY COUNT - MISSING PRINCIPAL
    // =========================================================

    @Test
    void getProjectActivityCount_whenPrincipalMissing_returns401()
            throws Exception {

        mockMvc.perform(
                get(
                        "/api/projects/{projectId}/activity/count",
                        PROJECT_ID
                )
        )
        .andExpect(
                status().isUnauthorized()
        );

        verify(
                projectActivityService,
                never()
        )
        .getProjectActivityCount(
                PROJECT_ID,
                EMAIL
        );
    }

    // =========================================================
    // PRINCIPAL HELPER
    // =========================================================

    /*
     * Filters are deliberately disabled for this WebMvcTest.
     *
     * Supplying Principal directly allows these tests to verify
     * that the controller forwards Principal.getName() to the
     * service as the authenticated user's email address.
     */
    private Principal principal() {

        return () ->
                EMAIL;
    }

    // =========================================================
    // RESPONSE TEST DATA
    // =========================================================

    private ProjectActivityResponse createActivityResponse() {

        ProjectActivityResponse response =
                new ProjectActivityResponse();

        response.setId(
                ACTIVITY_ID
        );

        response.setProjectId(
                PROJECT_ID
        );

        response.setTaskId(
                TASK_ID
        );

        response.setUserId(
                5L
        );

        response.setUserName(
                "Test User"
        );

        response.setUserEmail(
                EMAIL
        );

        response.setActivityType(
                ActivityType.TASK_STATUS_CHANGED
        );

        response.setFieldName(
                "status"
        );

        response.setOldValue(
                "OPEN"
        );

        response.setNewValue(
                "COMPLETED"
        );

        response.setDescription(
                "Changed task status from OPEN to COMPLETED."
        );

        response.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        8,
                        31,
                        14,
                        0
                )
        );

        return response;
    }
}