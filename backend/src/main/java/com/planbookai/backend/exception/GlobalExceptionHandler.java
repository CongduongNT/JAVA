package com.planbookai.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("wrong password") || msg.contains("user not found") || msg.contains("invalid token")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.contains("exists")) {
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity.status(status).body(response);
    }
}
