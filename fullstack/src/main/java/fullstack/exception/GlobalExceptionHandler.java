package fullstack.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    // =========================================================
    // JAKARTA BEAN VALIDATION
    //
    // Handles:
    //
    // @NotBlank
    // @Size
    // @Pattern
    // etc.
    //
    // Example:
    //
    // {
    //   "status": 400,
    //   "error": "Bad Request",
    //   "message": "Validation failed.",
    //   "errors": {
    //      "title": "Task title is required."
    //   }
    // }
    // =========================================================

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleValidationException(

                    MethodArgumentNotValidException exception,

                    HttpServletRequest request) {

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (
            FieldError fieldError :
            exception
                    .getBindingResult()
                    .getFieldErrors()
        ) {

            /*
             * Keep the first validation message
             * for each field.
             */
            fieldErrors.putIfAbsent(

                    fieldError.getField(),

                    fieldError.getDefaultMessage() == null
                            ? "Invalid value."
                            : fieldError.getDefaultMessage()
            );
        }

        ApiErrorResponse response =
                ApiErrorResponse.validation(

                        HttpStatus.BAD_REQUEST.value(),

                        HttpStatus.BAD_REQUEST
                                .getReasonPhrase(),

                        "Validation failed.",

                        request.getRequestURI(),

                        fieldErrors
                );

        return ResponseEntity
                .badRequest()
                .body(
                        response
                );
    }

    // =========================================================
    // ILLEGAL ARGUMENT
    //
    // Used by service/controller validation such as:
    //
    // - invalid sort field
    // - invalid page size
    // - invalid due-date filter
    // - invalid status
    // - invalid priority
    // =========================================================

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleIllegalArgument(

                    IllegalArgumentException exception,

                    HttpServletRequest request) {

        ApiErrorResponse response =
                ApiErrorResponse.of(

                        HttpStatus.BAD_REQUEST.value(),

                        HttpStatus.BAD_REQUEST
                                .getReasonPhrase(),

                        exception.getMessage(),

                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(
                        response
                );
    }

    // =========================================================
    // RESPONSE STATUS EXCEPTION
    //
    // Handles service errors such as:
    //
    // 401 Authentication required
    // 404 Project not found
    // 404 Task not found
    // =========================================================

    @ExceptionHandler(
            ResponseStatusException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleResponseStatusException(

                    ResponseStatusException exception,

                    HttpServletRequest request) {

        int status =
                exception
                        .getStatusCode()
                        .value();

        HttpStatus httpStatus =
                HttpStatus.resolve(
                        status
                );

        String errorName =
                httpStatus == null
                        ? "HTTP Error"
                        : httpStatus
                                .getReasonPhrase();

        String message =
                exception.getReason();

        if (
            message == null
            || message.isBlank()
        ) {

            message =
                    errorName;
        }

        ApiErrorResponse response =
                ApiErrorResponse.of(

                        status,

                        errorName,

                        message,

                        request.getRequestURI()
                );

        return ResponseEntity
                .status(
                        status
                )
                .body(
                        response
                );
    }

    // =========================================================
    // MALFORMED / UNREADABLE JSON
    //
    // Examples:
    //
    // malformed JSON
    // invalid LocalDate
    // incorrect property value format
    // =========================================================

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleUnreadableRequest(

                    HttpMessageNotReadableException exception,

                    HttpServletRequest request) {

        ApiErrorResponse response =
                ApiErrorResponse.of(

                        HttpStatus.BAD_REQUEST.value(),

                        HttpStatus.BAD_REQUEST
                                .getReasonPhrase(),

                        "Request body is invalid or malformed.",

                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(
                        response
                );
    }

    // =========================================================
    // UNEXPECTED SERVER ERROR
    //
    // Never send Java exception details or stack traces
    // back to React.
    //
    // Log the real exception on the server instead.
    // =========================================================

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<ApiErrorResponse>
            handleUnexpectedException(

                    Exception exception,

                    HttpServletRequest request) {

        logger.error(
                "Unexpected error while processing {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        ApiErrorResponse response =
                ApiErrorResponse.of(

                        HttpStatus.INTERNAL_SERVER_ERROR
                                .value(),

                        HttpStatus.INTERNAL_SERVER_ERROR
                                .getReasonPhrase(),

                        "An unexpected server error occurred.",

                        request.getRequestURI()
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        response
                );
    }
}