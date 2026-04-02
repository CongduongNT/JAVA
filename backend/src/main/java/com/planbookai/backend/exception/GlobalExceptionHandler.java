package com.planbookai.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler – Xử lý tập trung các exception trong ứng dụng.
 *
 * <p>Mỗi loại exception được map sang HTTP status phù hợp:
 * <ul>
 *   <li>{@link AIServiceException} → 502 Bad Gateway</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request (validation)</li>
 *   <li>{@link RuntimeException} → tự suy ra status từ message</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi từ Gemini AI (API không khả dụng, JSON không hợp lệ, v.v.).
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAIServiceException(AIServiceException e) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "AI_SERVICE_ERROR", e.getMessage());
    }

    /**
     * Xử lý lỗi validation từ @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "Malformed request body";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenOperationException(ForbiddenOperationException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage());
    }

    /**
     * Fallback – xử lý các RuntimeException chưa được bắt cụ thể.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (msg.contains("wrong password") || msg.contains("user not found") || msg.contains("invalid token")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.contains("exists") || msg.contains("duplicate")) {
            status = HttpStatus.CONFLICT;
        } else if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.contains("forbidden") || msg.contains("access denied")) {
            status = HttpStatus.FORBIDDEN;
        }

        return buildErrorResponse(status, "ERROR", e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
