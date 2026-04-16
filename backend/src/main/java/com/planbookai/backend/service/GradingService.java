package com.planbookai.backend.service;

import com.planbookai.backend.dto.*;
import com.planbookai.backend.exception.BadRequestException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.*;
import com.planbookai.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
// Collectors not used (using .toList() instead)

/**
 * GradingService – Xử lý nghiệp vụ chấm điểm bài thi từ OCR.
 *
 * <h2>Luồng gradeExam()</h2>
 * <ol>
 *   <li>Lấy toàn bộ exam_questions cho exam_id (đã có correct_answer)</li>
 *   <li>Lấy student_answers (OCR) cho student_id + exam_id</li>
 *   <li>Với mỗi câu: normalize → so sánh → tính điểm</li>
 *   <li>Upsert grading_results + grading_result_details</li>
 * </ol>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>BR-01: 1 student × 1 exam = 1 grading_result (upsert)</li>
 *   <li>BR-02: OCR blank → status BLANK, điểm 0</li>
 *   <li>BR-03: So sánh không case-sensitive, không phân biệt dấu</li>
 *   <li>BR-04: MULTIPLE_CHOICE lấy first-alpha-char</li>
 *   <li>BR-06: AI feedback không tự động lưu</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class GradingService {

    private static final Logger log = LoggerFactory.getLogger(GradingService.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final GradingResultRepository  gradingResultRepository;
    private final GradingResultDetailRepository detailRepository;
    private final ExamRepository           examRepository;
    private final ExamQuestionRepository  examQuestionRepository;
    private final GeminiAIService         geminiAIService;

    // =========================================================================
    // gradeExam – Chấm điểm từ OCR (gọi từ OCR System sau khi scan xong)
    // =========================================================================

    /**
     * Chấm điểm bài thi cho 1 học sinh.
     *
     * <p>Assumption: bảng student_answers đã tồn tại và có dữ liệu OCR.
     * Nếu chưa có → ném BadRequestException.
     *
     * <p>Luồng:
     * <ol>
     *   <li>Validate exam tồn tại và có câu hỏi</li>
     *   <li>Lấy exam_questions + correct_answers</li>
     *   <li>Lấy student_answers (OCR) cho student+exam</li>
     *   <li>So sánh từng câu → tính điểm</li>
     *   <li>Upsert grading_result + details</li>
     * </ol>
     *
     * @param studentId   ID học sinh
     * @param studentName Tên học sinh (denormalized)
     * @param studentCode Mã học sinh (nullable)
     * @param examId      ID bài thi
     * @param ocrAnswers  Map: questionId → StudentAnswerOCR
     * @return GradingResultDetailDTO đầy đủ
     */
    @Transactional
    public GradingResultDetailDTO gradeExam(
            Long studentId,
            String studentName,
            String studentCode,
            Long examId,
            Map<Long, StudentAnswerOCR> ocrAnswers) {

        // 1. Validate exam
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));

        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByOrderIndex(examId);
        if (examQuestions.isEmpty()) {
            throw new BadRequestException("Exam has no questions: " + examId);
        }

        // 2. Find or create grading_result (upsert)
        GradingResult result = gradingResultRepository
                .findByStudentIdAndExamId(studentId, examId)
                .orElseGet(() -> GradingResult.builder()
                        .studentId(studentId)
                        .studentName(studentName)
                        .studentCode(studentCode)
                        .exam(exam)
                        .teacher(exam.getTeacher())
                        .build());

        // 3. Reset counters for re-grade
        result.setCorrectCount(0);
        result.setWrongCount(0);
        result.setBlankCount(0);
        result.setTotalScore(BigDecimal.ZERO);
        result.setTotalPossible(BigDecimal.ZERO);
        result.setGradedAt(LocalDateTime.now());
        result.setGradedBy(GradingResult.GradedBy.SYSTEM);

        // 4. Delete old details before re-grade (JPQL delete, safe with uninitialized collection)
        if (result.getId() != null) {
            detailRepository.deleteByGradingResultId(result.getId());
        }

        // 5. Grade each question
        List<GradingResultDetail> savedDetails = new ArrayList<>();
        BigDecimal totalScore     = BigDecimal.ZERO;
        BigDecimal totalPossible  = BigDecimal.ZERO;
        int correctCount = 0, wrongCount = 0, blankCount = 0;

        for (ExamQuestion eq : examQuestions) {
            StudentAnswerOCR ocr = ocrAnswers.get(eq.getQuestion().getId());

            GradingResultDetail detail = gradeQuestion(eq, ocr);
            detail.setGradingResult(result);
            savedDetails.add(detailRepository.save(detail));

            totalScore    = totalScore.add(detail.getPointsEarned());
            totalPossible = totalPossible.add(detail.getPointsPossible());

            switch (detail.getResultStatus()) {
                case CORRECT -> correctCount++;
                case WRONG   -> wrongCount++;
                case BLANK   -> blankCount++;
                case PARTIAL -> wrongCount++; // PARTIAL counted as wrong for now
            }
        }

        // 6. Set computed fields
        result.setTotalScore(totalScore);
        result.setTotalPossible(totalPossible);
        result.setPercentage(
                totalPossible.compareTo(BigDecimal.ZERO) > 0
                        ? totalScore.divide(totalPossible, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO
        );
        result.setCorrectCount(correctCount);
        result.setWrongCount(wrongCount);
        result.setBlankCount(blankCount);

        result = gradingResultRepository.save(result);

        log.info("[GradingService] Graded exam={} student={}: {}/{}/{} correct/wrong/blank",
                examId, studentId, correctCount, wrongCount, blankCount);

        return toDetailDTO(result, savedDetails);
    }

    // =========================================================================
    // gradeQuestion – So sánh 1 câu
    // =========================================================================

    private GradingResultDetail gradeQuestion(ExamQuestion eq, StudentAnswerOCR ocr) {
        Question q    = eq.getQuestion();
        BigDecimal pts = eq.getPoints() != null ? eq.getPoints() : BigDecimal.ONE;

        String ocrText    = ocr != null ? ocr.answerText() : "";
        String correctText = q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "";

        String normOcr    = AnswerNormalizer.normalize(ocrText, q.getType());
        String normCorrect = AnswerNormalizer.normalizeCorrectAnswer(correctText, q.getType());

        boolean isCorrect = normOcr.equals(normCorrect) && !normOcr.isEmpty();
        boolean isBlank   = ocrText.trim().isEmpty();

        GradingResultDetail.ResultStatus status;
        BigDecimal earned;

        if (isBlank) {
            status = GradingResultDetail.ResultStatus.BLANK;
            earned = BigDecimal.ZERO;
        } else if (isCorrect) {
            status = GradingResultDetail.ResultStatus.CORRECT;
            earned = pts;
        } else {
            status = GradingResultDetail.ResultStatus.WRONG;
            earned = BigDecimal.ZERO;
        }

        return GradingResultDetail.builder()
                .examQuestion(eq)
                .questionId(q.getId())
                .ocrAnswerText(ocrText)
                .correctAnswerText(correctText)
                .resultStatus(status)
                .pointsEarned(earned)
                .pointsPossible(pts)
                .ocrConfidence(ocr != null ? ocr.confidence() : null)
                .build();
    }

    // =========================================================================
    // GET – Danh sách kết quả chấm
    // =========================================================================

    /**
     * GET /api/v1/grading-results?exam_id={id}
     * Phân trang, sắp xếp theo gradedAt DESC.
     */
    public PageResponse<GradingResultSummaryDTO> getGradingResultsByExam(
            Long examId, Long teacherId, int page, int size) {

        // Validate exam tồn tại và teacher có quyền
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        assertTeacherOwnsExam(exam, teacherId);

        Pageable pageable = PageRequest.of(page, size);
        Page<GradingResult> resultPage = gradingResultRepository.findByExamId(examId, pageable);

        return PageResponse.from(resultPage.map(this::toSummaryDTO));
    }

    /**
     * GET /api/v1/grading-results/{id}
     */
    public GradingResultDetailDTO getGradingResultDetail(Long resultId, Long teacherId) {
        GradingResult result = gradingResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Grading result not found: " + resultId));

        assertTeacherOwnsExam(result.getExam(), teacherId);

        List<GradingResultDetail> details =
                detailRepository.findByGradingResultIdOrderByExamQuestionOrderIndex(resultId);

        return toDetailDTO(result, details);
    }

    // =========================================================================
    // PUT – Cập nhật feedback
    // =========================================================================

    /**
     * PUT /api/v1/grading-results/{id}/feedback
     *
     * <p>Nếu requestAiFeedback = true → gọi AI sinh gợi ý.
     * teacherFeedback được lưu theo request (hoặc giữ nguyên nếu null).
     * feedbackSource = AI_EDITED nếu gọi AI, MANUAL nếu chỉ có teacherFeedback.
     */
    @Transactional
    public UpdateFeedbackResponse updateFeedback(
            Long resultId,
            UpdateFeedbackRequest request,
            Long teacherId) {

        GradingResult result = gradingResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Grading result not found: " + resultId));

        assertTeacherOwnsExam(result.getExam(), teacherId);

        String aiSuggestion = null;

        // Gọi AI feedback nếu requested
        if (request.isRequestAiFeedback()) {
            List<GradingResultDetail> details =
                    detailRepository.findByGradingResultIdOrderByExamQuestionOrderIndex(resultId);

            if (details.isEmpty()) {
                throw new BadRequestException("Exam has not been graded yet. Cannot request AI feedback.");
            }

            aiSuggestion = generateAiFeedback(result, details);
            result.setAiFeedbackSuggestion(aiSuggestion);
            result.setFeedbackSource(GradingResult.FeedbackSource.AI_EDITED);
            log.info("[GradingService] AI feedback generated for resultId={}", resultId);
        }

        // Cập nhật teacher feedback (null = giữ nguyên)
        if (request.getTeacherFeedback() != null) {
            result.setTeacherFeedback(request.getTeacherFeedback());
            result.setFeedbackSource(
                    request.isRequestAiFeedback()
                            ? GradingResult.FeedbackSource.AI_EDITED
                            : GradingResult.FeedbackSource.MANUAL
            );
        }

        result.setGradedBy(GradingResult.GradedBy.TEACHER);
        result.setLastUpdatedAt(LocalDateTime.now());
        gradingResultRepository.save(result);

        return UpdateFeedbackResponse.builder()
                .id(result.getId())
                .studentName(result.getStudentName())
                .teacherFeedback(result.getTeacherFeedback())
                .aiFeedbackSuggestion(result.getAiFeedbackSuggestion())
                .feedbackSource(result.getFeedbackSource() != null
                        ? result.getFeedbackSource().name() : null)
                .updatedAt(result.getLastUpdatedAt().format(DTF))
                .build();
    }

    // =========================================================================
    // AI Feedback Generation
    // =========================================================================

    private String generateAiFeedback(GradingResult result, List<GradingResultDetail> details) {
        Exam exam = result.getExam();

        List<GradingFeedbackRequest.WrongQuestionInfo> wrongQuestions = new ArrayList<>();
        List<GradingFeedbackRequest.BlankQuestionInfo> blankQuestions  = new ArrayList<>();

        for (GradingResultDetail d : details) {
            if (d.getResultStatus() == GradingResultDetail.ResultStatus.WRONG) {
                wrongQuestions.add(GradingFeedbackRequest.WrongQuestionInfo.builder()
                        .order(d.getExamQuestion().getOrderIndex())
                        .questionContent(d.getExamQuestion().getQuestion().getContent())
                        .studentAnswer(d.getOcrAnswerText())
                        .correctAnswer(d.getCorrectAnswerText())
                        .pointsEarned(d.getPointsEarned())
                        .pointsPossible(d.getPointsPossible())
                        .build());
            } else if (d.getResultStatus() == GradingResultDetail.ResultStatus.BLANK) {
                blankQuestions.add(GradingFeedbackRequest.BlankQuestionInfo.builder()
                        .order(d.getExamQuestion().getOrderIndex())
                        .questionContent(d.getExamQuestion().getQuestion().getContent())
                        .build());
            }
        }

        GradingFeedbackRequest aiRequest = GradingFeedbackRequest.builder()
                .examTitle(exam.getTitle())
                .subject(exam.getSubject())
                .gradeLevel(exam.getGradeLevel())
                .studentName(result.getStudentName())
                .totalScore(result.getTotalScore())
                .totalPossible(result.getTotalPossible())
                .percentage(result.getPercentage())
                .correctCount(result.getCorrectCount())
                .wrongCount(result.getWrongCount())
                .blankCount(result.getBlankCount())
                .wrongQuestions(wrongQuestions)
                .blankQuestions(blankQuestions)
                .build();

        return geminiAIService.generateFeedback(aiRequest);
    }

    // =========================================================================
    // DTO Mapping
    // =========================================================================

    private GradingResultSummaryDTO toSummaryDTO(GradingResult r) {
        return GradingResultSummaryDTO.builder()
                .id(r.getId())
                .studentId(r.getStudentId())
                .studentName(r.getStudentName())
                .studentCode(r.getStudentCode())
                .examId(r.getExam().getId())
                .totalScore(r.getTotalScore())
                .totalPossible(r.getTotalPossible())
                .percentage(r.getPercentage())
                .correctCount(r.getCorrectCount())
                .wrongCount(r.getWrongCount())
                .blankCount(r.getBlankCount())
                .hasFeedback(r.getTeacherFeedback() != null && !r.getTeacherFeedback().isBlank())
                .gradedAt(r.getGradedAt())
                .build();
    }

    private GradingResultDetailDTO toDetailDTO(GradingResult r, List<GradingResultDetail> details) {
        List<GradingResultDetailDTO.GradingQuestionDetailDTO> detailDTOs = details.stream()
                .map(this::toQuestionDetailDTO)
                .toList();

        return GradingResultDetailDTO.builder()
                .id(r.getId())
                .studentId(r.getStudentId())
                .studentName(r.getStudentName())
                .studentCode(r.getStudentCode())
                .examId(r.getExam().getId())
                .examTitle(r.getExam().getTitle())
                .totalScore(r.getTotalScore())
                .totalPossible(r.getTotalPossible())
                .percentage(r.getPercentage())
                .correctCount(r.getCorrectCount())
                .wrongCount(r.getWrongCount())
                .blankCount(r.getBlankCount())
                .teacherFeedback(r.getTeacherFeedback())
                .aiFeedbackSuggestion(r.getAiFeedbackSuggestion())
                .feedbackSource(r.getFeedbackSource() != null ? r.getFeedbackSource().name() : null)
                .gradedAt(r.getGradedAt())
                .gradedBy(r.getGradedBy() != null ? r.getGradedBy().name() : null)
                .details(detailDTOs)
                .build();
    }

    private GradingResultDetailDTO.GradingQuestionDetailDTO toQuestionDetailDTO(GradingResultDetail d) {
        return GradingResultDetailDTO.GradingQuestionDetailDTO.builder()
                .id(d.getId())
                .examQuestionId(d.getExamQuestion().getId())
                .questionId(d.getQuestionId())
                .orderIndex(d.getExamQuestion().getOrderIndex())
                .questionContent(d.getExamQuestion().getQuestion().getContent())
                .questionType(d.getExamQuestion().getQuestion().getType().name())
                .ocrAnswerText(d.getOcrAnswerText())
                .correctAnswerText(d.getCorrectAnswerText())
                .resultStatus(d.getResultStatus().name())
                .pointsEarned(d.getPointsEarned())
                .pointsPossible(d.getPointsPossible())
                .ocrConfidence(d.getOcrConfidence())
                .build();
    }

    // =========================================================================
    // Validation Helpers
    // =========================================================================

    private void assertTeacherOwnsExam(Exam exam, Long teacherId) {
        if (exam.getTeacher() != null && exam.getTeacher().getId().equals(teacherId)) {
            return; // ok
        }
        // TODO: check ADMIN/MANAGER role from SecurityContext when needed
        throw new com.planbookai.backend.exception.ForbiddenOperationException(
                "You do not have access to this exam");
    }

    // =========================================================================
    // Nested static class – OCR answer record (đọc từ student_answers table)
    // =========================================================================

    /**
     * Record đại diện cho 1 đáp án OCR từ bảng student_answers.
     * OCR Service sẽ implement/calls gradeExam với Map<questionId, StudentAnswerOCR>.
     */
    public record StudentAnswerOCR(
            String answerText,
            BigDecimal confidence
    ) {}
}
