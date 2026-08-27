package fullstack.controller;

import fullstack.dto.AuthResponse;
import fullstack.dto.LoginRequest;
import fullstack.dto.RegisterRequest;
import fullstack.dto.UserResponse;

import fullstack.model.AppUser;

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

    public AuthController(
            AuthService authService) {

        this.authService =
            authService;
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
     * Small local response type used
     * only for login failures.
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