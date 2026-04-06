-- ============================================================
-- Migration V3: Thêm cột approved_by vào bảng questions
-- Ticket: KAN-15 – Content Approval – Manager duyệt câu hỏi
-- ============================================================

ALTER TABLE questions
    ADD COLUMN approved_by BIGINT NULL AFTER is_approved,
    ADD CONSTRAINT fk_questions_approved_by
        FOREIGN KEY (approved_by) REFERENCES users(id)
        ON DELETE SET NULL;
