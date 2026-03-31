package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy cập dữ liệu cho bảng question_banks.
 */
@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Integer> {

    /** Tìm tất cả ngân hàng câu hỏi theo người tạo. */
    List<QuestionBank> findByCreatedById(Long userId);

    /** Tìm ngân hàng đã publish. */
    List<QuestionBank> findByIsPublishedTrue();
}
