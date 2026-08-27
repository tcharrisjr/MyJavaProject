package fullstack.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import fullstack.service.ProjectService;

import org.springframework.test.context.ActiveProfiles;


/**
 * =============================================================
 * SECURITY INTEGRATION TEST
 * =============================================================
 *
 * Tests the application's real Spring Security configuration.
 *
 * Unlike ProjectControllerIntegrationTest, this test loads:
 *
 * - SecurityConfig
 * - JwtAuthenticationFilter
 * - JwtService
 * - UserService
 * - CustomAuthenticationEntryPoint
 * - CustomAccessDeniedHandler
 *
 * The goal is to verify that protected application endpoints
 * behave correctly when authentication is:
 *
 * 1. Missing
 * 2. Supplied through Spring Security Test
 * 3. Supplied using an invalid JWT bearer token
 *
 * =============================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {


    // =========================================================
    // MOCK MVC
    // =========================================================

    @Autowired
    private MockMvc mockMvc;


    // =========================================================
    // PROJECT SERVICE
    //
    // Security is the focus of this test class.
    //
    // Mocking ProjectService prevents successful requests from
    // depending on project/database behavior.
    // =========================================================

    @MockitoBean
    private ProjectService projectService;


    // =========================================================
    // TEST 1
    //
    // NO AUTHENTICATION
    //
    // GET /api/projects
    //
    // Expected:
    //
    // 401 Unauthorized
    // =========================================================

    @Test
    void getProjects_withoutAuthentication_returnsUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isUnauthorized()
        );
    }


    // =========================================================
    // TEST 2
    //
    // AUTHENTICATED USER
    //
    // Spring Security Test directly creates an authenticated
    // SecurityContext.
    //
    // GET /api/projects
    //
    // Expected:
    //
    // 200 OK
    // =========================================================

    @Test
    void getProjects_withAuthenticatedUser_isAllowed()
            throws Exception {

        mockMvc.perform(

                get("/api/projects")

                        .with(
                                user(
                                        "testuser@example.com"
                                )
                        )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isOk()
        );
    }


    // =========================================================
    // TEST 3
    //
    // INVALID JWT
    //
    // GET /api/projects
    //
    // Authorization:
    //
    // Bearer this-is-not-a-valid-jwt
    //
    // JwtAuthenticationFilter should attempt to process the
    // token.
    //
    // JwtService should reject the malformed token.
    //
    // JwtAuthenticationFilter catches the JWT exception,
    // clears the SecurityContext, and continues the filter
    // chain.
    //
    // Because /api/projects/** requires authentication,
    // Spring Security should ultimately return:
    //
    // 401 Unauthorized
    // =========================================================

    @Test
    void getProjects_withInvalidJwt_returnsUnauthorized()
            throws Exception {

        mockMvc.perform(

                get("/api/projects")

                        .header(
                                "Authorization",
                                "Bearer this-is-not-a-valid-jwt"
                        )
        )

        .andDo(
                print()
        )

        .andExpect(
                status().isUnauthorized()
        );
    }
}