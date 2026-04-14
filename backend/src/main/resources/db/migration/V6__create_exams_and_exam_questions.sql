-- ============================================================
-- EXAMS & EXAM_QUESTIONS  (KAN-23)
-- Migration V6: Tạo bảng lưu đề thi và câu hỏi trong đề
-- ============================================================

CREATE TABLE IF NOT EXISTS exams (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    teacher_id      BIGINT          NOT NULL,
    title           VARCHAR(512)    NOT NULL,
    subject         VARCHAR(100),
    grade_level     VARCHAR(50),
    topic           VARCHAR(255),
    total_questions INT             NOT NULL DEFAULT 0,
    duration_mins   INT,
    randomized      BOOLEAN         NOT NULL DEFAULT FALSE,
    version_count   INT             NOT NULL DEFAULT 1,
    status          ENUM('DRAFT','PUBLISHED','CLOSED') NOT NULL DEFAULT 'DRAFT',
    ai_generated    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- exam_questions – Bảng trung gian giữa exams và questions.
-- Lưu thứ tự câu hỏi (order_index), phiên bản đề (version_number)
-- và điểm từng câu (points).
-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS exam_questions (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    exam_id         BIGINT          NOT NULL,
    question_id     BIGINT          NOT NULL,
    order_index     INT             NOT NULL DEFAULT 0,
    version_number  INT             NOT NULL DEFAULT 1,
    points          DECIMAL(5,2)    NOT NULL DEFAULT 1.00,
    FOREIGN KEY (exam_id)     REFERENCES exams(id)     ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)  ON DELETE CASCADE,
    UNIQUE KEY uq_exam_question_order (exam_id, order_index)
);

-- Index hỗ trợ truy vấn câu hỏi trong đề theo thứ tự
CREATE INDEX idx_exam_questions_exam_id ON exam_questions (exam_id, order_index);
