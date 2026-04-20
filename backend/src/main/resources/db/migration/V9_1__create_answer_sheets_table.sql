-- ============================================================
-- ANSWER SHEETS
-- Migration V9: create answer_sheets table
-- ============================================================

CREATE TABLE IF NOT EXISTS answer_sheets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_name VARCHAR(255),
    student_code VARCHAR(100),
    file_url VARCHAR(512) NOT NULL,
    ocr_status ENUM('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
    ocr_raw_data JSON,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SET @answer_sheets_exam_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'answer_sheets'
      AND constraint_name = 'fk_answer_sheets_exam'
      AND constraint_type = 'FOREIGN KEY'
);

SET @add_answer_sheets_exam_fk_sql = IF(
    @answer_sheets_exam_fk_exists = 0,
    'ALTER TABLE answer_sheets ADD CONSTRAINT fk_answer_sheets_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE',
    'SELECT 1'
);

PREPARE stmt FROM @add_answer_sheets_exam_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @answer_sheets_teacher_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'answer_sheets'
      AND constraint_name = 'fk_answer_sheets_teacher'
      AND constraint_type = 'FOREIGN KEY'
);

SET @add_answer_sheets_teacher_fk_sql = IF(
    @answer_sheets_teacher_fk_exists = 0,
    'ALTER TABLE answer_sheets ADD CONSTRAINT fk_answer_sheets_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE',
    'SELECT 1'
);

PREPARE stmt FROM @add_answer_sheets_teacher_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @answer_sheets_exam_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'answer_sheets'
      AND index_name = 'idx_answer_sheets_exam_id'
);

SET @create_answer_sheets_exam_idx_sql = IF(
    @answer_sheets_exam_idx_exists = 0,
    'CREATE INDEX idx_answer_sheets_exam_id ON answer_sheets (exam_id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_answer_sheets_exam_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @answer_sheets_teacher_uploaded_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'answer_sheets'
      AND index_name = 'idx_answer_sheets_teacher_uploaded_at'
);

SET @create_answer_sheets_teacher_uploaded_idx_sql = IF(
    @answer_sheets_teacher_uploaded_idx_exists = 0,
    'CREATE INDEX idx_answer_sheets_teacher_uploaded_at ON answer_sheets (teacher_id, uploaded_at, id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_answer_sheets_teacher_uploaded_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @answer_sheets_teacher_status_uploaded_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'answer_sheets'
      AND index_name = 'idx_answer_sheets_teacher_status_uploaded_at'
);

SET @create_answer_sheets_teacher_status_uploaded_idx_sql = IF(
    @answer_sheets_teacher_status_uploaded_idx_exists = 0,
    'CREATE INDEX idx_answer_sheets_teacher_status_uploaded_at ON answer_sheets (teacher_id, ocr_status, uploaded_at, id)',
    'SELECT 1'
);

PREPARE stmt FROM @create_answer_sheets_teacher_status_uploaded_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
