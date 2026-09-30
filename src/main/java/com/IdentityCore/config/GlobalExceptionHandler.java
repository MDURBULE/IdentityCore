package com.IdentityCore.config;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.IdentityCore.model.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

// @RestControllerAdvice
@org.springframework.stereotype.Component
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        String requestId = getRequestId(req);
        String code = ex.getMessage() != null ? ex.getMessage() : "INVALID_ARGUMENT";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(code, ex.getMessage(), requestId));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        String requestId = getRequestId(req);
        String code = ex.getMessage() != null ? ex.getMessage() : "ILLEGAL_STATE";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(code, ex.getMessage(), requestId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String requestId = getRequestId(req);
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_FAILED", sb.toString(), requestId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest req) {
        String requestId = getRequestId(req);
        Config.getLgr().error("Unhandled exception for request: {}", requestId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", requestId));
    }

    private String getRequestId(HttpServletRequest req) {
        String reqId = req.getHeader("X-Request-Id");
        return reqId != null ? reqId : UUID.randomUUID().toString();
    }
}
