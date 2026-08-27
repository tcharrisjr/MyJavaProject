package fullstack.security;

import java.util.Arrays;
import java.util.List;

import fullstack.service.UserService;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final UserService
            userService;

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final CustomAuthenticationEntryPoint
            customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler
            customAccessDeniedHandler;


    // =========================================================
    // CONFIGURABLE CORS ORIGINS
    // =========================================================
    //
    // Development default:
    //
    // 5173 - 5176
    //     Vite development servers
    //
    // 4173 - 4174
    //     Vite production preview servers
    //
    // Production:
    //
    // Override using:
    //
    // APP_CORS_ALLOWED_ORIGINS
    //
    // Example:
    //
    // https://projects.example.com
    //
    // Multiple origins may be comma-separated.
    //
    // =========================================================

    private final String
            allowedOrigins;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SecurityConfig(

            UserService userService,

            JwtAuthenticationFilter jwtAuthenticationFilter,

            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,

            CustomAccessDeniedHandler customAccessDeniedHandler,

            @Value(
                "${app.cors.allowed-origins:"
                + "http://localhost:5173,"
                + "http://localhost:5174,"
                + "http://localhost:5175,"
                + "http://localhost:5176,"
                + "http://localhost:4173,"
                + "http://localhost:4174}"
            )
            String allowedOrigins) {

        this.userService =
                userService;

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.customAuthenticationEntryPoint =
                customAuthenticationEntryPoint;

        this.customAccessDeniedHandler =
                customAccessDeniedHandler;

        this.allowedOrigins =
                allowedOrigins;
    }


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public static PasswordEncoder
            passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }


    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public DaoAuthenticationProvider
            authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }


    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager
            authenticationManager() {

        return new ProviderManager(
                authenticationProvider()
        );
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain
            securityFilterChain(
                    HttpSecurity http)

            throws Exception {

        http

            /*
             * =================================================
             * CSRF
             * =================================================
             *
             * This application uses JWT bearer tokens.
             *
             * Authentication is not based on an HTTP session
             * or authentication cookie, so CSRF protection is
             * disabled for this REST API.
             * =================================================
             */
            .csrf(
                csrf ->
                    csrf.disable()
            )


            /*
             * =================================================
             * CORS
             * =================================================
             */
            .cors(
                cors ->
                    cors.configurationSource(
                        corsConfigurationSource()
                    )
            )


            /*
             * =================================================
             * SESSION MANAGEMENT
             * =================================================
             *
             * JWT authentication is stateless.
             * =================================================
             */
            .sessionManagement(
                session ->
                    session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                    )
            )


            /*
             * =================================================
             * AUTHORIZATION
             * =================================================
             */
            .authorizeHttpRequests(
                authorize ->
                    authorize

                        /*
                         * -------------------------------------
                         * CORS PREFLIGHT
                         * -------------------------------------
                         */
                        .requestMatchers(
                            HttpMethod.OPTIONS,
                            "/**"
                        )
                        .permitAll()


                        /*
                         * -------------------------------------
                         * PUBLIC AUTHENTICATION ENDPOINTS
                         * -------------------------------------
                         */
                        .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login"
                        )
                        .permitAll()


                        /*
                         * -------------------------------------
                         * CURRENT USER
                         * -------------------------------------
                         */
                        .requestMatchers(
                            "/api/auth/me"
                        )
                        .authenticated()


                        /*
                         * -------------------------------------
                         * PROJECT API
                         * -------------------------------------
                         */
                        .requestMatchers(
                            "/api/projects/**"
                        )
                        .authenticated()


                        /*
                         * -------------------------------------
                         * TASK API
                         * -------------------------------------
                         */
                        .requestMatchers(
                            "/api/tasks/**"
                        )
                        .authenticated()


                        /*
                         * -------------------------------------
                         * EVERYTHING ELSE
                         * -------------------------------------
                         *
                         * Secure by default.
                         * -------------------------------------
                         */
                        .anyRequest()
                        .authenticated()
            )


            /*
             * =================================================
             * AUTHENTICATION PROVIDER
             * =================================================
             */
            .authenticationProvider(
                authenticationProvider()
            )


            /*
             * =================================================
             * STANDARDIZED SECURITY ERRORS
             * =================================================
             *
             * HTTP 401
             *     CustomAuthenticationEntryPoint
             *
             * HTTP 403
             *     CustomAccessDeniedHandler
             * =================================================
             */
            .exceptionHandling(
                exceptions ->
                    exceptions

                        .authenticationEntryPoint(
                            customAuthenticationEntryPoint
                        )

                        .accessDeniedHandler(
                            customAccessDeniedHandler
                        )
            )


            /*
             * =================================================
             * JWT FILTER
             * =================================================
             *
             * JWT processing must happen before Spring's
             * username/password authentication filter.
             * =================================================
             */
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );


        return http.build();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource
            corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        // =====================================================
        // ALLOWED ORIGINS
        // =====================================================
        //
        // Convert the comma-separated configuration property
        // into a clean List<String>.
        //
        // =====================================================

        List<String> origins =
                Arrays.stream(
                        allowedOrigins.split(",")
                )
                .map(
                        String::trim
                )
                .filter(
                        origin ->
                            !origin.isBlank()
                )
                .toList();


        configuration.setAllowedOrigins(
                origins
        );


        // =====================================================
        // ALLOWED HTTP METHODS
        // =====================================================

        configuration.setAllowedMethods(
                List.of(
                    "GET",
                    "POST",
                    "PUT",
                    "DELETE",
                    "OPTIONS"
                )
        );


        // =====================================================
        // ALLOWED REQUEST HEADERS
        // =====================================================

        configuration.setAllowedHeaders(
                List.of(
                    "Authorization",
                    "Content-Type"
                )
        );


        // =====================================================
        // EXPOSED RESPONSE HEADERS
        // =====================================================

        configuration.setExposedHeaders(
                List.of(
                    "Authorization"
                )
        );


        // =====================================================
        // CREDENTIALS
        // =====================================================
        //
        // Authentication uses:
        //
        // Authorization: Bearer <JWT>
        //
        // We are not using a cross-origin authentication
        // cookie, so credentials do not need to be enabled.
        //
        // =====================================================

        configuration.setAllowCredentials(
                false
        );


        // =====================================================
        // PREFLIGHT CACHE
        // =====================================================
        //
        // Browser may cache successful CORS preflight results
        // for one hour.
        //
        // =====================================================

        configuration.setMaxAge(
                3600L
        );


        // =====================================================
        // REGISTER CORS CONFIGURATION
        // =====================================================

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }
}