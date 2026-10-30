package com.pulsefit.attendanceservice.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(
            ResponseStatusException e,
            WebRequest request) {

        int status = e.getStatusCode().value();

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put(
                "message",
                e.getReason() == null
                        ? "Request failed"
                        : e.getReason()
        );
        body.put("path", getPath(request));

        return ResponseEntity
                .status(status)
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(
            MethodArgumentNotValidException e,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );
        body.put("message", "Validation failed");
        body.put("errors", errors);
        body.put("path", getPath(request));

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> other(
            Exception e,
            WebRequest request) {

        /*
         * The actual exception is still printed by Spring
         * in the service terminal. The API response remains
         * safe and consistent.
         */
        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put(
                "status",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        body.put("message", "Internal server error");
        body.put("path", getPath(request));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    private String getPath(WebRequest request) {
        return request
                .getDescription(false)
                .replace("uri=", "");
    }
}