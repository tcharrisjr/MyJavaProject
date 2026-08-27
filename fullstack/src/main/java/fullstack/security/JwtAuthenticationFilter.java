package fullstack.security;

import fullstack.model.AppUser;
import fullstack.service.UserService;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService
        jwtService;

    private final UserService
        userService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserService userService) {

        this.jwtService =
            jwtService;

        this.userService =
            userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException,
                   IOException {

        String authorizationHeader =
            request.getHeader(
                "Authorization"
            );

        /*
         * No bearer token:
         * continue through filter chain.
         */
        if (
            authorizationHeader == null
            ||
            !authorizationHeader
                .startsWith(
                    "Bearer "
                )
        ) {
            filterChain.doFilter(
                request,
                response
            );

            return;
        }

        String token =
            authorizationHeader
                .substring(7);

        try {

            String email =
                jwtService
                    .extractUsername(
                        token
                    );

            if (
                email != null
                &&
                SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    == null
            ) {

                AppUser user =
                    (AppUser)
                    userService
                        .loadUserByUsername(
                            email
                        );

                if (
                    jwtService
                        .isTokenValid(
                            token,
                            user
                        )
                ) {

                    UsernamePasswordAuthenticationToken
                        authentication =
                            new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                            );

                    authentication
                        .setDetails(
                            new WebAuthenticationDetailsSource()
                                .buildDetails(
                                    request
                                )
                        );

                    SecurityContext context =
                        SecurityContextHolder
                            .createEmptyContext();

                    context.setAuthentication(
                        authentication
                    );

                    SecurityContextHolder
                        .setContext(
                            context
                        );
                }
            }

        } catch (
            JwtException |
            IllegalArgumentException exception
        ) {

            /*
             * Invalid or expired token.
             *
             * Leave the SecurityContext
             * unauthenticated.
             */
            SecurityContextHolder
                .clearContext();
        }

        filterChain.doFilter(
            request,
            response
        );
    }
}