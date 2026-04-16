package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.GradingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository truy cập dữ liệu cho bảng grading_results.
 */
@Repository
public interface GradingResultRepository extends JpaRepository<GradingResult, Long> {

    /**
     * Tìm kết quả chấm của 1 học sinh cho 1 bài thi (dùng cho upsert).
     */
    Optional<GradingResult> findByStudentIdAndExamId(Long studentId, Long examId);

    /**
     * Danh sách kết quả chấm theo exam_id (phân trang).
     */
    Page<GradingResult> findByExamId(Long examId, Pageable pageable);

    /**
     * Kiểm tra grading_result có tồn tại không.
     */
    boolean existsByStudentIdAndExamId(Long studentId, Long examId);
}
