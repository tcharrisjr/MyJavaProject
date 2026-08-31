package fullstack.controller;

import java.util.Comparator;
import java.util.List;

import fullstack.dto.AuthResponse;
import fullstack.dto.LoginRequest;
import fullstack.dto.RegisterRequest;
import fullstack.dto.UserResponse;

import fullstack.model.AppUser;

import fullstack.repository.UserRepository;

import fullstack.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins =
                "http://localhost:5173"
)
public class AuthController {

    private final AuthService
            authService;

    /*
     * Sequence 13A
     *
     * Used to retrieve users who can be
     * selected as task assignees.
     */
    private final UserRepository
            userRepository;

    public AuthController(
            AuthService authService,
            UserRepository userRepository) {

        this.authService =
                authService;

        this.userRepository =
                userRepository;
    }

    /*
     * =====================================================
     * REGISTER
     * =====================================================
     */

    @PostMapping("/register")
    public ResponseEntity<AuthResponse>
        register(
            @Valid
            @RequestBody
            RegisterRequest request) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        authService
                                .register(
                                        request
                                )
                );
    }

    /*
     * =====================================================
     * LOGIN
     * =====================================================
     */

    @PostMapping("/login")
    public ResponseEntity<?>
        login(
            @Valid
            @RequestBody
            LoginRequest request) {

        try {

            return ResponseEntity.ok(
                    authService
                            .login(
                                    request
                            )
            );

        } catch (
                BadCredentialsException
                        exception
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            new AuthenticationError(
                                    "Invalid email or password."
                            )
                    );
        }
    }

    /*
     * =====================================================
     * CURRENT USER
     * =====================================================
     */

    @GetMapping("/me")
    public ResponseEntity<UserResponse>
        getCurrentUser(
            @AuthenticationPrincipal
            AppUser user) {

        return ResponseEntity.ok(
                UserResponse.from(
                        user
                )
        );
    }

    /*
     * =====================================================
     * SEQUENCE 13A - TASK ASSIGNEES
     * =====================================================
     *
     * Returns enabled application users that may be selected
     * as task assignees.
     *
     * The endpoint deliberately returns UserResponse rather
     * than AppUser so password/security fields are never
     * exposed to the frontend.
     *
     * Example:
     *
     * GET /api/auth/assignees
     *
     * [
     *   {
     *     "id": 1,
     *     "name": "Test User",
     *     "email": "test@example.com",
     *     "role": "USER"
     *   }
     * ]
     * =====================================================
     */

    @GetMapping("/assignees")
    public ResponseEntity<List<UserResponse>>
        getAssignees() {

        List<UserResponse> assignees =
                userRepository
                        .findAll()
                        .stream()

                        /*
                         * Do not offer disabled accounts
                         * as task assignees.
                         */
                        .filter(
                                AppUser::isEnabled
                        )

                        /*
                         * Sort alphabetically for a cleaner
                         * dropdown on the React UI.
                         */
                        .sorted(
                                Comparator.comparing(
                                        AppUser::getName,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                        )

                        /*
                         * Convert entities into safe DTOs.
                         */
                        .map(
                                UserResponse::from
                        )

                        .toList();

        return ResponseEntity.ok(
                assignees
        );
    }

    /*
     * =====================================================
     * LOGIN ERROR RESPONSE
     * =====================================================
     *
     * Small local response type used
     * only for login failures.
     * =====================================================
     */

    private static class
        AuthenticationError {

        private final String message;

        public AuthenticationError(
                String message) {

            this.message =
                    message;
        }

        public String getMessage() {

            return message;
        }
    }
}