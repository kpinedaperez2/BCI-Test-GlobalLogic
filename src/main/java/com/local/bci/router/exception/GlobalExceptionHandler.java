package com.local.bci.router.exception;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, 400, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleConflict(IllegalStateException ex) {
        return buildError(HttpStatus.CONFLICT, 409, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildError(HttpStatus.BAD_REQUEST, 400, detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        log.error("Unhandled error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal server error");
    }

    private ResponseEntity<?> buildError(org.springframework.http.HttpStatus status, int code, String detail) {
        var err = new ErrorItem(Instant.now().toString(), code, detail);
        return ResponseEntity.status(status)
                .body(Collections.singletonMap("error", List.of(err)));
    }

    @Data
    static class ErrorItem {
        private final String timestamp;
        private final int codigo;
        private final String detail;
    }
}
