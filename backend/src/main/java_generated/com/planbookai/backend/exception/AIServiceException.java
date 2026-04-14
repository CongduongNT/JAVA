package com.planbookai.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * AIServiceException – ném khi Gemini AI không khả dụng hoặc trả về dữ liệu không hợp lệ.
 *
 * <p>Được xử lý bởi {@code GlobalExceptionHandler} và trả về HTTP 502 Bad Gateway.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class AIServiceException extends RuntimeException {

    public AIServiceException(String message) {
        super(message);
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
