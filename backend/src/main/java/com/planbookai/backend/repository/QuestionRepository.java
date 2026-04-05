package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy cập dữ liệu cho bảng questions.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Lấy tất cả câu hỏi thuộc 1 ngân hàng. */
    List<Question> findByBankId(Integer bankId);

    /** Lấy câu hỏi theo ngân hàng và topic. */
    List<Question> findByBankIdAndTopic(Integer bankId, String topic);

    /** Lấy câu hỏi theo trạng thái phê duyệt (dùng cho Manager duyệt). */
    List<Question> findByIsApproved(Boolean isApproved);
}
