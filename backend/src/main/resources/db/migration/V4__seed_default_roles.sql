-- ============================================================
-- DEFAULT ROLES
-- Migration V4: dữ liệu nền tối thiểu cho phân quyền
-- ============================================================

INSERT INTO roles (name, description)
SELECT 'ADMIN', 'Default system role for ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = 'ADMIN'
);

INSERT INTO roles (name, description)
SELECT 'MANAGER', 'Default system role for MANAGER'
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = 'MANAGER'
);

INSERT INTO roles (name, description)
SELECT 'STAFF', 'Default system role for STAFF'
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = 'STAFF'
);

INSERT INTO roles (name, description)
SELECT 'TEACHER', 'Default system role for TEACHER'
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = 'TEACHER'
);
