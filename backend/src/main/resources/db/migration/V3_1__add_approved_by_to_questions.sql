-- ============================================================
-- QUESTIONS APPROVAL
-- Migration V3.1: add approved_by to questions
-- ============================================================

SET @approved_by_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'questions'
      AND column_name = 'approved_by'
);

SET @approved_by_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'questions'
      AND constraint_name = 'fk_questions_approved_by'
      AND constraint_type = 'FOREIGN KEY'
);

SET @add_approved_by_column_sql = IF(
    @approved_by_column_exists = 0,
    'ALTER TABLE questions ADD COLUMN approved_by BIGINT NULL AFTER is_approved',
    'SELECT 1'
);

PREPARE stmt FROM @add_approved_by_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_approved_by_fk_sql = IF(
    @approved_by_fk_exists = 0,
    'ALTER TABLE questions ADD CONSTRAINT fk_questions_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL',
    'SELECT 1'
);

PREPARE stmt FROM @add_approved_by_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
