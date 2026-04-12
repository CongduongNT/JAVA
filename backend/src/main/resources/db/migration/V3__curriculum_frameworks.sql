-- ============================================================
-- CURRICULUM FRAMEWORKS
-- Migration V3: Tạo bảng curriculum_frameworks cho Admin quản lý template kế hoạch bài dạy
-- Task: KAN-17 - Curriculum Framework Admin quản lý template
-- ============================================================

CREATE TABLE IF NOT EXISTS curriculum_frameworks (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(255) NOT NULL COMMENT 'Tiêu đề template framework',
    subject         VARCHAR(100) COMMENT 'Môn học',
    grade_level     VARCHAR(50) COMMENT 'Cấp lớp/khối',
    description     TEXT COMMENT 'Mô tả chi tiết về framework',
    structure       JSON COMMENT 'Cấu trúc template: objectives, activities, assessment, ...',
    created_by      BIGINT COMMENT 'ID admin tạo template',
    is_published    BOOLEAN DEFAULT FALSE COMMENT 'Trạng thái công bố',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu template kế hoạch bài dạy (curriculum frameworks)';

-- Index cho tìm kiếm nhanh
CREATE INDEX idx_frameworks_subject ON curriculum_frameworks(subject);
CREATE INDEX idx_frameworks_grade ON curriculum_frameworks(grade_level);
CREATE INDEX idx_frameworks_published ON curriculum_frameworks(is_published);
CREATE INDEX idx_frameworks_created_by ON curriculum_frameworks(created_by);
