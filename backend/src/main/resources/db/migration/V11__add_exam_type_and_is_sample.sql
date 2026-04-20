-- V11: Add exam_type to exams table (EXERCISE vs EXAM)
-- Add is_sample flag to lesson_plans and questions for Staff-created content

ALTER TABLE exams
    ADD COLUMN exam_type VARCHAR(20) NOT NULL DEFAULT 'EXAM'
        COMMENT 'EXAM = đề thi chính thức, EXERCISE = bài tập luyện tập';

ALTER TABLE lesson_plans
    ADD COLUMN is_sample BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'TRUE nếu là giáo án mẫu do Staff tạo';

ALTER TABLE questions
    ADD COLUMN is_sample BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'TRUE nếu là câu hỏi mẫu do Staff tạo';

-- Index for filtering
CREATE INDEX idx_exams_exam_type ON exams (exam_type);
CREATE INDEX idx_lesson_plans_is_sample ON lesson_plans (is_sample);
CREATE INDEX idx_questions_is_sample ON questions (is_sample);
