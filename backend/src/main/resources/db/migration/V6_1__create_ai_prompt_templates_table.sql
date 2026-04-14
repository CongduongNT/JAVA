CREATE TABLE ai_prompt_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    purpose VARCHAR(50) NOT NULL, -- Ví dụ: 'QUESTION_GEN', 'LESSON_PLAN_GEN'
    prompt_text TEXT NOT NULL,
    variables VARCHAR(512), -- Lưu danh sách biến cách nhau bởi dấu phẩy, ví dụ: "subject,grade,topic"
    status VARCHAR(20) DEFAULT 'PENDING', -- Trạng thái: 'PENDING', 'APPROVED', 'REJECTED'
    created_by BIGINT,
    approved_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prompt_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_prompt_approver FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_prompt_purpose (purpose),
    INDEX idx_prompt_status (status)
);

