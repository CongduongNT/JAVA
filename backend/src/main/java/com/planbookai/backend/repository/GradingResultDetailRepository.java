package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.GradingResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy cập dữ liệu cho bảng grading_result_details.
 */
@Repository
public interface GradingResultDetailRepository extends JpaRepository<GradingResultDetail, Long> {

    /**
     * Lấy toàn bộ chi tiết của 1 grading_result, theo thứ tự câu hỏi.
     */
    List<GradingResultDetail> findByGradingResultIdOrderByExamQuestionOrderIndex(Long gradingResultId);

    /**
     * Xóa toàn bộ chi tiết của 1 grading_result (dùng trước khi re-grade).
     * Dùng JPQL thay vì orphanRemoval để đảm bảo xóa ngay cả khi collection chưa initialized.
     */
    @Modifying
    @Query("DELETE FROM GradingResultDetail d WHERE d.gradingResult.id = :resultId")
    void deleteByGradingResultId(@Param("resultId") Long resultId);
}
