-- Lấy ID của tài khoản Admin/User đầu tiên để làm người tạo
SET @admin_id = (SELECT id FROM users LIMIT 1);

-- Xóa dữ liệu cũ nếu trùng tiêu đề để tránh lặp khi chạy lại (tùy chọn)
DELETE FROM ai_prompt_templates WHERE title IN ('Tạo câu hỏi trắc nghiệm', 'Tạo bài tập tự luận', 'Soạn giáo án mẫu');

-- Chèn dữ liệu mẫu trạng thái APPROVED
INSERT INTO ai_prompt_templates (title, purpose, prompt_text, variables, status, created_by, approved_by, created_at)
VALUES 
(
    'Tạo câu hỏi trắc nghiệm', 
    'QUESTION_GEN', 
    'Bạn là một chuyên gia giáo dục. Hãy tạo 5 câu hỏi trắc nghiệm về chủ đề {{topic}} với độ khó mức độ {{level}}. Yêu cầu mỗi câu hỏi có 4 phương án lựa chọn và có đáp án giải thích chi tiết.', 
    'topic,level', 
    'APPROVED', @admin_id, @admin_id, NOW()
),
(
    'Tạo bài tập tự luận', 
    'QUESTION_GEN', 
    'Hãy soạn 3 bài tập tự luận tự duy cao về nội dung {{topic}} dành cho học sinh lớp 10. Mức độ yêu cầu: {{level}}.', 
    'topic,level', 
    'APPROVED', @admin_id, @admin_id, NOW()
),
(
    'Soạn giáo án mẫu', 
    'LESSON_PLAN_GEN', 
    'Hãy lập kế hoạch bài dạy chi tiết cho bài {{topic}}. Mức độ nhận thức: {{level}}.', 
    'topic,level', 
    'APPROVED', @admin_id, @admin_id, NOW()
);