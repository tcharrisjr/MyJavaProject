package fullstack.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(

            HttpServletRequest request,

            HttpServletResponse response,

            AuthenticationException authException)

            throws IOException,
                   ServletException {

        response.setStatus(
                HttpStatus.UNAUTHORIZED.value()
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        String path =
                escapeJson(
                        request.getRequestURI()
                );

        String timestamp =
                java.time.LocalDateTime
                        .now()
                        .toString();

        String json =
                """
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Authentication is required.",
                  "path": "%s",
                  "errors": null
                }
                """.formatted(
                        timestamp,
                        path
                );

        response
                .getWriter()
                .write(
                        json
                );
    }

    // =========================================================
    // BASIC JSON STRING ESCAPING
    // =========================================================

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\r",
                        "\\r"
                );
    }
}