-- ============================================================
-- EXAMS & EXAM QUESTIONS
-- Migration V9: Tạo bảng lưu trữ đề thi và chi tiết câu hỏi trong đề
-- ============================================================

CREATE TABLE IF NOT EXISTS exams (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    title               VARCHAR(255) NOT NULL,
    subject             VARCHAR(100),
    grade_level         VARCHAR(50),
    description         TEXT,
    duration_minutes    INT DEFAULT 45,
    total_points        DECIMAL(5,2) DEFAULT 10.0,
    created_by          BIGINT NOT NULL,
    status              ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    is_ai_generated     BOOLEAN DEFAULT FALSE,
    settings            JSON, -- Lưu các cấu hình bổ sung như trộn đề, hiển thị lời giải
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS exam_questions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id         BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    order_index     INT NOT NULL, -- Thứ tự câu hỏi trong đề
    points          DECIMAL(5,2) DEFAULT 1.0, -- Điểm số riêng cho câu hỏi này trong đề
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Thêm index để tối ưu hóa việc truy vấn đề thi theo giáo viên và môn học
CREATE INDEX idx_exams_created_by ON exams(created_by);
CREATE INDEX idx_exams_subject ON exams(subject);

-- Thêm index cho bảng trung gian để tăng tốc độ load đề thi
CREATE INDEX idx_exam_questions_exam_id ON exam_questions(exam_id);
CREATE INDEX idx_exam_questions_order ON exam_questions(exam_id, order_index);
