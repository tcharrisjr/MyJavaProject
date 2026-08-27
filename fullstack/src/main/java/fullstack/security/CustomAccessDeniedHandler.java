package fullstack.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    @Override
    public void handle(

            HttpServletRequest request,

            HttpServletResponse response,

            AccessDeniedException accessDeniedException)

            throws IOException,
                   ServletException {

        response.setStatus(
                HttpStatus.FORBIDDEN.value()
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
                  "status": 403,
                  "error": "Forbidden",
                  "message": "Access is denied.",
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