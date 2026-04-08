package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            select q
            from Question q
            where q.bank.id = :bankId
              and (:topic is null or coalesce(lower(q.topic), '') like lower(concat('%', :topic, '%')))
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:type is null or q.type = :type)
            """)
    Page<Question> findByBankIdWithFilters(
            @Param("bankId") Integer bankId,
            @Param("topic") String topic,
            @Param("difficulty") Question.Difficulty difficulty,
            @Param("type") Question.QuestionType type,
            Pageable pageable);
}
