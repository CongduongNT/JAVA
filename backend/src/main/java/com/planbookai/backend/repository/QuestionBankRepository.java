package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy cập dữ liệu cho bảng question_bank.
 */
@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Integer> {

    /** Tìm tất cả ngân hàng câu hỏi theo người tạo. */
    List<QuestionBank> findByCreatedById(Long userId);

    /** Tìm ngân hàng đã publish. */
    List<QuestionBank> findByIsPublishedTrue();

    /**
     * Tìm bank đầu tiên của teacher (dùng làm fallback khi AI questions
     * cần lưu vào bank nhưng không có bank_id trong request).
     */
    Optional<QuestionBank> findFirstByCreatedById(Long userId);
}
