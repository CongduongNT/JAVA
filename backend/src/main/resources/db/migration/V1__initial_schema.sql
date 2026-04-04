-- ============================================================
-- USERS & ROLES
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        ENUM('ADMIN','MANAGER','STAFF','TEACHER') NOT NULL UNIQUE,
    description TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id          INT NOT NULL,
    full_name        VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(512) NOT NULL,
    phone            VARCHAR(20),
    avatar_url       VARCHAR(512),
    is_active        BOOLEAN DEFAULT TRUE,
    email_verified   BOOLEAN DEFAULT FALSE,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ============================================================
-- SUBSCRIPTION & ORDERS
-- ============================================================

CREATE TABLE IF NOT EXISTS subscription_packages (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price           DECIMAL(12,2) NOT NULL,
    duration_days   INT NOT NULL,
    features        JSON,
    is_active       BOOLEAN DEFAULT TRUE,
    created_by      BIGINT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    package_id      INT NOT NULL,
    status          ENUM('PENDING','ACTIVE','EXPIRED','CANCELLED') DEFAULT 'PENDING',
    amount_paid     DECIMAL(12,2),
    payment_method  VARCHAR(100),
    started_at      DATETIME,
    expires_at      DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES subscription_packages(id)
);

-- ============================================================
-- QUESTIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS questions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    type            ENUM('MULTIPLE_CHOICE','FILL_IN_BLANK','SHORT_ANSWER') NOT NULL,
    difficulty      ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'MEDIUM',
    created_by      BIGINT NOT NULL,
    content         TEXT NOT NULL,
    topic           VARCHAR(255),
    options         JSON,
    correct_answer  TEXT NOT NULL,
    explanation     TEXT,
    ai_generated    BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
