package com.planbookai.backend.config;

import com.google.genai.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * GeminiAIConfig – Cấu hình Google Gemini AI client.
 *
 * <p>Đọc API key từ property {@code gemini.api-key} (inject từ biến môi trường GEMINI_API_KEY).
 * Tạo một bean {@link Client} singleton dùng chung cho toàn bộ ứng dụng.
 *
 * <p>Cách dùng: inject {@code Client geminiClient} vào service cần gọi AI.
 *
 * <p>Nếu {@code apiKey} trống, bean này sẽ không được khởi tạo ở startup do lazy initialization.
 * Các endpoint AI sẽ tự kiểm tra cấu hình và trả lỗi rõ ràng khi được gọi.
 */
@Configuration
public class GeminiAIConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIConfig.class);

    @Value("${gemini.api-key:}")
    private String apiKey;

    /**
     * Tạo Gemini AI Client.
     *
     * <p>Client được cấu hình với API key từ application.yml.
     * Bean được đánh dấu lazy để không chặn quá trình boot nếu môi trường chưa cấu hình AI.
     *
     * @return Google GenAI Client instance
     */
    @Bean
    @Lazy
    public Client geminiClient() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GeminiAI] GEMINI_API_KEY is not configured. Gemini client bean will stay unavailable until AI is configured.");
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }
        log.info("[GeminiAI] Gemini client initialized successfully.");
        return new Client.Builder()
                .apiKey(apiKey)
                .build();
    }
}
