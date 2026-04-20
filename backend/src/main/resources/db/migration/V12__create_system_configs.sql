-- V12: System configuration table for Admin
CREATE TABLE system_configs (
    config_key   VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(500) NOT NULL,
    description  VARCHAR(255),
    updated_by   VARCHAR(255),
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO system_configs (config_key, config_value, description, updated_by) VALUES
('max_questions_per_exam',    '100',  'Maximum number of questions allowed per exam',         'system'),
('max_file_size_mb',          '10',   'Maximum OCR upload file size in megabytes',            'system'),
('ai_timeout_seconds',        '30',   'Gemini AI API call timeout in seconds',                'system'),
('ocr_max_files_batch',       '30',   'Maximum files per OCR batch upload',                   'system'),
('allow_teacher_registration','true', 'Allow teachers to self-register without admin approval','system'),
('default_exam_duration_mins','45',   'Default exam duration in minutes',                     'system'),
('question_ai_batch_size',    '10',   'Number of questions AI generates per batch call',      'system');
