-- ============================================================
-- LESSON PLANS
-- Migration V6: create lesson_plans table
-- ============================================================

CREATE TABLE IF NOT EXISTS lesson_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    framework_id INT,
    title VARCHAR(255) NOT NULL,
    subject VARCHAR(100),
    grade_level VARCHAR(50),
    topic VARCHAR(255),
    objectives TEXT,
    activities TEXT,
    assessment TEXT,
    materials TEXT,
    duration_minutes INT,
    ai_generated BOOLEAN DEFAULT FALSE,
    status ENUM('DRAFT','PUBLISHED') DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lesson_plans_teacher
        FOREIGN KEY (teacher_id) REFERENCES users(id)
);

SET @lesson_plans_teacher_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND constraint_name = 'fk_lesson_plans_teacher'
      AND constraint_type = 'FOREIGN KEY'
);

SET @add_teacher_fk_sql = IF(
    @lesson_plans_teacher_fk_exists = 0,
    'ALTER TABLE lesson_plans ADD CONSTRAINT fk_lesson_plans_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)',
    'SELECT 1'
);

PREPARE stmt FROM @add_teacher_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @lesson_plans_teacher_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND index_name = 'idx_lesson_plans_teacher_id'
);

SET @create_teacher_idx_sql = IF(
    @lesson_plans_teacher_idx_exists = 0,
    'CREATE INDEX idx_lesson_plans_teacher_id ON lesson_plans (teacher_id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_teacher_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @lesson_plans_framework_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND index_name = 'idx_lesson_plans_framework_id'
);

SET @create_framework_idx_sql = IF(
    @lesson_plans_framework_idx_exists = 0,
    'CREATE INDEX idx_lesson_plans_framework_id ON lesson_plans (framework_id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_framework_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @lesson_plans_status_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND index_name = 'idx_lesson_plans_status'
);

SET @create_status_idx_sql = IF(
    @lesson_plans_status_idx_exists = 0,
    'CREATE INDEX idx_lesson_plans_status ON lesson_plans (status)',
    'SELECT 1'
);

PREPARE stmt FROM @create_status_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @curriculum_frameworks_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'curriculum_frameworks'
);

SET @lesson_plans_framework_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND constraint_name = 'fk_lesson_plans_framework'
      AND constraint_type = 'FOREIGN KEY'
);

SET @add_framework_fk_sql = IF(
    @curriculum_frameworks_exists > 0 AND @lesson_plans_framework_fk_exists = 0,
    'ALTER TABLE lesson_plans ADD CONSTRAINT fk_lesson_plans_framework FOREIGN KEY (framework_id) REFERENCES curriculum_frameworks(id)',
    'SELECT 1'
);

PREPARE stmt FROM @add_framework_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
