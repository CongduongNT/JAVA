-- ============================================================
-- QUESTION BANK RENAME
-- Migration V3: đổi tên bảng question_bank -> question_banks
-- ============================================================

SET @question_bank_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'question_bank'
);

SET @question_banks_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'question_banks'
);

SET @rename_sql = IF(
    @question_bank_exists > 0 AND @question_banks_exists = 0,
    'RENAME TABLE question_bank TO question_banks',
    'SELECT 1'
);

PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
