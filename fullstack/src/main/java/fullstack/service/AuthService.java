package fullstack.service;

import fullstack.dto.AuthResponse;
import fullstack.dto.LoginRequest;
import fullstack.dto.RegisterRequest;
import fullstack.dto.UserResponse;

import fullstack.model.AppUser;

import fullstack.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager
        authenticationManager;

    private final UserService
        userService;

    private final JwtService
        jwtService;

    public AuthService(
            AuthenticationManager
                authenticationManager,

            UserService
                userService,

            JwtService
                jwtService) {

        this.authenticationManager =
            authenticationManager;

        this.userService =
            userService;

        this.jwtService =
            jwtService;
    }

    /*
     * =====================================================
     * REGISTER
     * =====================================================
     */

    public AuthResponse register(
            RegisterRequest request) {

        AppUser user =
            userService
                .register(
                    request
                );

        String token =
            jwtService
                .generateToken(
                    user
                );

        return buildResponse(
            user,
            token
        );
    }

    /*
     * =====================================================
     * LOGIN
     * =====================================================
     */

    public AuthResponse login(
            LoginRequest request) {

        String email =
            request
                .getEmail()
                .trim()
                .toLowerCase();

        /*
         * Throws BadCredentialsException
         * if credentials are invalid.
         */
        authenticationManager
            .authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    request.getPassword()
                )
            );

        AppUser user =
            userService
                .getByEmail(
                    email
                );

        String token =
            jwtService
                .generateToken(
                    user
                );

        return buildResponse(
            user,
            token
        );
    }

    /*
     * =====================================================
     * RESPONSE
     * =====================================================
     */

    private AuthResponse buildResponse(
            AppUser user,
            String token) {

        return new AuthResponse(
            token,

            jwtService
                .getExpirationMs(),

            UserResponse
                .from(user)
        );
    }
}