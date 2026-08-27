package fullstack.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import fullstack.security.JwtAuthenticationFilter;

import fullstack.service.ProjectService;


/**
 * =============================================================
 * PROJECT CONTROLLER MVC TEST
 * =============================================================
 *
 * Tests the ProjectController MVC layer independently from
 * the production JWT security implementation.
 *
 * This test verifies:
 *
 * 1. Controller endpoint mappings
 * 2. Authentication username propagation
 * 3. ProjectService delegation
 * 4. HTTP response status
 * 5. JSON response serialization
 *
 * -------------------------------------------------------------
 * WHY SECURITY FILTERS ARE DISABLED
 * -------------------------------------------------------------
 *
 * The production application uses:
 *
 * - SecurityConfig
 * - JwtAuthenticationFilter
 * - JwtService
 * - UserService
 * - CustomAuthenticationEntryPoint
 * - CustomAccessDeniedHandler
 *
 * Those components will be tested separately in dedicated
 * security integration tests.
 *
 * This test focuses specifically on ProjectController.
 *
 * @AutoConfigureMockMvc(addFilters = false)
 *
 * prevents the Spring Security filter chain from running
 * during these controller tests.
 *
 * JwtAuthenticationFilter is also excluded from the MVC test
 * component scan because @WebMvcTest can discover Filter
 * implementations.
 *
 * Authentication is supplied directly to MockMvc as the
 * request principal.
 *
 * =============================================================
 */
@WebMvcTest(
        controllers = ProjectController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerIntegrationTest {


    // =========================================================
    // TEST USER
    // =========================================================

    private static final String TEST_USER_EMAIL =
            "testuser@example.com";


    // =========================================================
    // MOCK MVC
    //
    // Provides HTTP-style testing of Spring MVC without
    // starting a real Tomcat server.
    // =========================================================

    @Autowired
    private MockMvc mockMvc;


    // =========================================================
    // PROJECT SERVICE
    //
    // The service is mocked so this class tests only the
    // controller/MVC layer.
    // =========================================================

    @MockitoBean
    private ProjectService projectService;


    // =========================================================
    // HELPER
    //
    // Creates an authenticated Principal that implements
    // Spring Security's Authentication interface.
    //
    // ProjectController receives this object through its
    // Authentication method parameter.
    // =========================================================

    private Authentication authenticatedUser() {

        return new UsernamePasswordAuthenticationToken(
                TEST_USER_EMAIL,
                null,
                Collections.emptyList()
        );
    }


    // =========================================================
    // TEST 1
    //
    // AUTHENTICATED USER CAN GET PROJECTS
    //
    // GET /api/projects
    // =========================================================

    @Test
    void getProjects_authenticatedUser_returnsOk()
            throws Exception {

        // -----------------------------------------------------
        // ARRANGE
        //
        // Simulate a user who currently has no projects.
        // -----------------------------------------------------

        when(
                projectService
                        .getProjectsForUser(
                                TEST_USER_EMAIL
                        )
        )
        .thenReturn(
                Collections.emptyList()
        );


        // -----------------------------------------------------
        // ACT + ASSERT
        //
        // Supply Authentication directly as the request
        // Principal.
        //
        // Expected:
        //
        // HTTP 200 OK
        // JSON response []
        // -----------------------------------------------------

        mockMvc.perform(

                get("/api/projects")

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


        // -----------------------------------------------------
        // VERIFY
        //
        // Confirm that ProjectController extracted the
        // authenticated username and passed it to the service.
        // -----------------------------------------------------

        verify(
                projectService
        )
        .getProjectsForUser(
                TEST_USER_EMAIL
        );
    }


    // =========================================================
    // TEST 2
    //
    // AUTHENTICATED USER CAN GET GLOBAL PROJECT STATISTICS
    //
    // GET /api/projects/stats
    // =========================================================

    @Test
    void getProjectStats_authenticatedUser_returnsOk()
            throws Exception {

        // -----------------------------------------------------
        // ARRANGE
        //
        // Simulate dashboard/project statistics returned by
        // ProjectService.
        // -----------------------------------------------------

        Map<String, Long> stats =
                Map.of(
                        "projects", 3L,
                        "totalTasks", 10L,
                        "open", 4L,
                        "inProgress", 3L,
                        "completed", 3L
                );


        when(
                projectService
                        .getProjectStats(
                                TEST_USER_EMAIL
                        )
        )
        .thenReturn(
                stats
        );


        // -----------------------------------------------------
        // ACT + ASSERT
        //
        // GET /api/projects/stats
        //
        // Expected:
        //
        // HTTP 200 OK
        // JSON statistics returned by ProjectService
        // -----------------------------------------------------

        mockMvc.perform(

                get("/api/projects/stats")

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
                jsonPath("$.projects")
                        .value(3)
        )

        .andExpect(
                jsonPath("$.totalTasks")
                        .value(10)
        )

        .andExpect(
                jsonPath("$.open")
                        .value(4)
        )

        .andExpect(
                jsonPath("$.inProgress")
                        .value(3)
        )

        .andExpect(
                jsonPath("$.completed")
                        .value(3)
        );


        // -----------------------------------------------------
        // VERIFY
        //
        // Confirm that the authenticated username was passed
        // to ProjectService.
        // -----------------------------------------------------

        verify(
                projectService
        )
        .getProjectStats(
                TEST_USER_EMAIL
        );
    }
}