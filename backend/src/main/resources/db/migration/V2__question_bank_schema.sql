-- ============================================================
-- QUESTION BANK & QUESTIONS
-- Migration V2: Tạo bảng question_bank và questions
-- ============================================================

CREATE TABLE IF NOT EXISTS question_bank (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    subject         VARCHAR(100),
    grade_level     VARCHAR(50),
    description     TEXT,
    created_by      BIGINT,
    is_published    BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS questions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_id         INT NOT NULL,
    created_by      BIGINT NOT NULL,
    content         TEXT NOT NULL,
    type            ENUM('MULTIPLE_CHOICE','SHORT_ANSWER','FILL_IN_BLANK') NOT NULL,
    difficulty      ENUM('EASY','MEDIUM','HARD') DEFAULT 'MEDIUM',
    topic           VARCHAR(255),
    options         JSON,
    correct_answer  TEXT,
    explanation     TEXT,
    ai_generated    BOOLEAN DEFAULT FALSE,
    is_approved     BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES question_bank(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
