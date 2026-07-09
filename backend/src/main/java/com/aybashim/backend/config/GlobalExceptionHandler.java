package com.aybashim.backend.config;

import com.aybashim.backend.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, ex.getReason(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, WebRequest request) {
        String path = request instanceof ServletWebRequest servletWebRequest
                ? servletWebRequest.getRequest().getRequestURI()
                : "";

        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message == null || message.isBlank() ? status.getReasonPhrase() : message,
                path
        );

        return ResponseEntity.status(status).body(error);
    }
}
