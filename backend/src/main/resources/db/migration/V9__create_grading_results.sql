-- ============================================================
-- GRADING RESULTS  (KAN-25)
-- Migration V9: Tạo bảng lưu kết quả chấm và chi tiết từng câu
-- ============================================================

-- ----------------------------------------------------------------
-- grading_results – Tổng hợp kết quả chấm của 1 học sinh cho 1 bài thi.
-- 1 student × 1 exam = 1 row (upsert on re-grade)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS grading_results (
    id                      BIGINT           PRIMARY KEY AUTO_INCREMENT,
    student_id              BIGINT           NOT NULL,
    student_name            VARCHAR(255)     NOT NULL,
    student_code            VARCHAR(100),
    exam_id                 BIGINT           NOT NULL,
    teacher_id              BIGINT           NOT NULL,

    total_score             DECIMAL(10,4)   NOT NULL DEFAULT 0,
    total_possible          DECIMAL(10,4)   NOT NULL DEFAULT 0,
    percentage              DECIMAL(5,2)    NOT NULL DEFAULT 0,

    correct_count           INT              NOT NULL DEFAULT 0,
    wrong_count             INT              NOT NULL DEFAULT 0,
    blank_count             INT              NOT NULL DEFAULT 0,

    teacher_feedback        TEXT,
    ai_feedback_suggestion  TEXT,
    feedback_source         ENUM('MANUAL', 'AI_EDITED') DEFAULT NULL,

    graded_at               DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    graded_by               ENUM('SYSTEM', 'TEACHER') NOT NULL DEFAULT 'SYSTEM',
    last_updated_at         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Constraints & Indexes
    UNIQUE KEY uq_student_exam (student_id, exam_id),
    INDEX idx_grading_results_exam_id (exam_id),
    INDEX idx_grading_results_student_id (student_id),
    FOREIGN KEY (exam_id)      REFERENCES exams(id)      ON DELETE CASCADE,
    FOREIGN KEY (teacher_id)  REFERENCES users(id)
);

-- ----------------------------------------------------------------
-- grading_result_details – Chi tiết từng câu của 1 kết quả chấm.
-- 1 row = 1 câu hỏi trong bài làm của học sinh.
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS grading_result_details (
    id                      BIGINT           PRIMARY KEY AUTO_INCREMENT,
    grading_result_id       BIGINT           NOT NULL,
    exam_question_id        BIGINT           NOT NULL,
    question_id             BIGINT           NOT NULL,

    ocr_answer_text         TEXT,
    correct_answer_text     TEXT,

    result_status           ENUM('CORRECT', 'WRONG', 'BLANK', 'PARTIAL') NOT NULL,
    points_earned           DECIMAL(5,2)    NOT NULL DEFAULT 0,
    points_possible         DECIMAL(5,2)    NOT NULL DEFAULT 0,

    ocr_confidence          DECIMAL(4,3),

    created_at              DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints & Indexes
    UNIQUE KEY uq_grading_detail (grading_result_id, exam_question_id),
    INDEX idx_grading_details_grading_result_id (grading_result_id),
    FOREIGN KEY (grading_result_id) REFERENCES grading_results(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_question_id)  REFERENCES exam_questions(id)
);
