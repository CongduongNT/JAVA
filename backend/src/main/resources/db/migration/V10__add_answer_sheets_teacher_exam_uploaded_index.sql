-- ============================================================
-- ANSWER SHEETS
-- Migration V10: add composite index for teacher + exam listing
-- ============================================================

SET @answer_sheets_teacher_exam_uploaded_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'answer_sheets'
      AND index_name = 'idx_answer_sheets_teacher_exam_uploaded_at'
);

SET @create_answer_sheets_teacher_exam_uploaded_idx_sql = IF(
    @answer_sheets_teacher_exam_uploaded_idx_exists = 0,
    'CREATE INDEX idx_answer_sheets_teacher_exam_uploaded_at ON answer_sheets (teacher_id, exam_id, uploaded_at, id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_answer_sheets_teacher_exam_uploaded_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
