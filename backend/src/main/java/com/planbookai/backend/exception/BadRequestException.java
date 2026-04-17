package com.planbookai.backend.exception;

/**
 * BadRequestException – 400 Bad Request
 * Dùng khi request data không hợp lệ hoặc violated business rule.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
