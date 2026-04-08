-- ============================================================
-- LESSON PLANS LISTING INDEXES
-- Migration V7: optimize lesson plan list queries
-- ============================================================

SET @lesson_plans_teacher_updated_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND index_name = 'idx_lesson_plans_teacher_updated_id'
);

SET @create_teacher_updated_idx_sql = IF(
    @lesson_plans_teacher_updated_idx_exists = 0,
    'CREATE INDEX idx_lesson_plans_teacher_updated_id ON lesson_plans (teacher_id, updated_at, id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_teacher_updated_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @lesson_plans_teacher_status_updated_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND index_name = 'idx_lesson_plans_teacher_status_updated_id'
);

SET @create_teacher_status_updated_idx_sql = IF(
    @lesson_plans_teacher_status_updated_idx_exists = 0,
    'CREATE INDEX idx_lesson_plans_teacher_status_updated_id ON lesson_plans (teacher_id, status, updated_at, id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_teacher_status_updated_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
