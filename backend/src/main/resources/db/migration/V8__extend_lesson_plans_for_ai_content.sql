SET @framework_code_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'framework_code'
);

SET @add_framework_code_sql = IF(
    @framework_code_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN framework_code VARCHAR(50) NULL AFTER framework_id',
    'SELECT 1'
);

PREPARE stmt FROM @add_framework_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ai_objectives_json_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'ai_objectives_json'
);

SET @add_ai_objectives_json_sql = IF(
    @ai_objectives_json_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN ai_objectives_json TEXT NULL AFTER duration_minutes',
    'SELECT 1'
);

PREPARE stmt FROM @add_ai_objectives_json_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ai_materials_json_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'ai_materials_json'
);

SET @add_ai_materials_json_sql = IF(
    @ai_materials_json_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN ai_materials_json TEXT NULL AFTER ai_objectives_json',
    'SELECT 1'
);

PREPARE stmt FROM @add_ai_materials_json_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @lesson_flow_json_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'lesson_flow_json'
);

SET @add_lesson_flow_json_sql = IF(
    @lesson_flow_json_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN lesson_flow_json TEXT NULL AFTER ai_materials_json',
    'SELECT 1'
);

PREPARE stmt FROM @add_lesson_flow_json_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @assessment_json_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'assessment_json'
);

SET @add_assessment_json_sql = IF(
    @assessment_json_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN assessment_json TEXT NULL AFTER lesson_flow_json',
    'SELECT 1'
);

PREPARE stmt FROM @add_assessment_json_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @homework_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'homework'
);

SET @add_homework_sql = IF(
    @homework_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN homework TEXT NULL AFTER assessment_json',
    'SELECT 1'
);

PREPARE stmt FROM @add_homework_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @notes_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'lesson_plans'
      AND column_name = 'notes'
);

SET @add_notes_sql = IF(
    @notes_exists = 0,
    'ALTER TABLE lesson_plans ADD COLUMN notes TEXT NULL AFTER homework',
    'SELECT 1'
);

PREPARE stmt FROM @add_notes_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
