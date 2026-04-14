package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy cập dữ liệu cho bảng exam_questions.
 */
@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    /** Lấy tất cả câu hỏi trong một đề thi, theo thứ tự. */
    List<ExamQuestion> findByExamIdOrderByOrderIndex(Long examId);

    /** Xóa tất cả câu hỏi khỏi đề thi. */
    void deleteByExamId(Long examId);
}
