package com.planbookai.backend.service;

import com.planbookai.backend.dto.AIGenerateExamRequest;
import com.planbookai.backend.dto.ExamDTO;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.*;
import com.planbookai.backend.repository.ExamQuestionRepository;
import com.planbookai.backend.repository.ExamRepository;
import com.planbookai.backend.repository.QuestionBankRepository;
import com.planbookai.backend.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ExamService – Xử lý nghiệp vụ tạo, quản lý và sinh đề thi bằng AI.
 *
 * <h2>Luồng aiGenerateExam()</h2>
 * <ol>
 *   <li><b>Lấy từ bank:</b> Truy vấn ngân hàng câu hỏi theo {@code bankIds},
 *       lọc theo môn học, chủ đề, độ khó, loại câu hỏi, và trạng thái đã duyệt.</li>
 *   <li><b>Trộn ngẫu nhiên &amp; cắt:</b> Shuffle danh sách câu từ bank, lấy tối đa
 *       {@code totalQuestions} câu.</li>
 *   <li><b>Tính số câu còn thiếu (gap):</b>
 *       {@code gap = totalQuestions - bankQuestionsSelected.size()}</li>
 *   <li><b>Sinh bù bằng AI:</b> Nếu {@code gap > 0}, gọi {@link GeminiAIService#generateExamGapQuestions}
 *       với danh sách nội dung câu đã có để tránh trùng lặp.</li>
 *   <li><b>Lưu câu AI vào bank:</b> Câu AI-generated được lưu vào ngân hàng câu hỏi
 *       (bank đầu tiên trong danh sách, hoặc bank mặc định của teacher).</li>
 *   <li><b>Tạo đề thi:</b> Tạo {@link Exam}, gắn tất cả câu hỏi theo thứ tự,
 *       đánh dấu nguồn gốc (BANK / AI) trong metadata.</li>
 *   <li><b>Trả về {@link ExamDTO}:</b> Gồm danh sách câu hỏi đầy đủ với metadata.</li>
 * </ol>
 */
@Service
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuestionService questionService;
    private final GeminiAIService geminiAIService;

    public ExamService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            QuestionRepository questionRepository,
            QuestionBankRepository questionBankRepository,
            QuestionService questionService,
            GeminiAIService geminiAIService) {
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionBankRepository = questionBankRepository;
        this.questionService = questionService;
        this.geminiAIService = geminiAIService;
    }

    // =========================================================================
    // READ
    // =========================================================================

    /**
     * Lấy danh sách đề của một teacher (phân trang, lọc theo môn + status).
     *
     * @param teacher Teacher đang đăng nhập
     * @param page    Trang (0-indexed)
     * @param size    Kích thước trang
     * @param subject Lọc theo môn (null = tất cả)
     * @param status  Lọc theo status (null = tất cả)
     * @return Trang kết quả ExamDTO (không kèm danh sách câu hỏi)
     */
    public com.planbookai.backend.dto.PageResponse<ExamDTO> getMyExams(
            User teacher,
            int page,
            int size,
            String subject,
            String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Exam.ExamStatus examStatus = parseStatus(status);

        Page<Exam> examPage = examRepository.findByTeacherWithFilters(
                teacher.getId(), subject, examStatus, pageable);

        return com.planbookai.backend.dto.PageResponse.from(examPage.map(this::toSummaryDTO));
    }

    /**
     * Xem chi tiết một đề thi (kèm danh sách câu hỏi đầy đủ).
     *
     * @param examId  ID đề thi
     * @param teacher Teacher hiện tại
     * @return ExamDTO đầy đủ
     */
    public ExamDTO getExamDetail(Long examId, User teacher) {
        Exam exam = findExamOrThrow(examId);
        assertOwner(exam, teacher);
        return toDetailDTO(exam);
    }

    // =========================================================================
    // AI GENERATE EXAM  (KAN-23)
    // =========================================================================

    /**
     * Sinh đề thi tự động bằng cách kết hợp câu hỏi từ bank và AI.
     *
     * <p>Đây là phương thức cốt lõi của KAN-23. Xem JavaDoc lớp để biết luồng xử lý.
     *
     * @param request Tham số sinh đề
     * @param teacher Giáo viên yêu cầu
     * @return ExamDTO đầy đủ với danh sách câu hỏi (nguồn gốc BANK + AI)
     */
    @Transactional
    public ExamDTO aiGenerateExam(AIGenerateExamRequest request, User teacher) {

        int total = request.getTotalQuestions();

        // ----------------------------------------------------------------
        // STEP 1: Lấy câu hỏi từ ngân hàng (nếu có bankIds)
        // ----------------------------------------------------------------
        List<Question> bankQuestions = fetchFromBank(request);

        log.info("[ExamService][aiGenerateExam] Bank returned {} questions (need {})",
                bankQuestions.size(), total);

        // ----------------------------------------------------------------
        // STEP 2: Trộn ngẫu nhiên và cắt theo số câu cần dùng
        // ----------------------------------------------------------------
        Collections.shuffle(bankQuestions);
        List<Question> selectedFromBank = bankQuestions.stream()
                .limit(total)
                .toList();

        // ----------------------------------------------------------------
        // STEP 3: Tính số câu còn thiếu (gap)
        // ----------------------------------------------------------------
        int gap = total - selectedFromBank.size();
        log.info("[ExamService][aiGenerateExam] Gap = {} (will be AI-generated)", gap);

        // ----------------------------------------------------------------
        // STEP 4: Sinh câu bù bằng Gemini AI (nếu gap > 0)
        // ----------------------------------------------------------------
        List<Question> aiQuestions = new ArrayList<>();
        if (gap > 0) {
            List<String> existingContents = selectedFromBank.stream()
                    .map(Question::getContent)
                    .toList();

            List<QuestionDTO> aiDTOs = geminiAIService.generateExamGapQuestions(
                    request.getSubject(),
                    request.getTopic(),
                    request.getDifficulty(),
                    request.getQuestionType(),
                    gap,
                    existingContents);

            // STEP 5: Lưu câu AI vào ngân hàng câu hỏi
            // lấy bank đầu tiên trong danh sách, hoặc bank mặc định (id=1)
            Integer targetBankId = (request.getBankIds() != null && !request.getBankIds().isEmpty())
                    ? request.getBankIds().get(0)
                    : null;

            aiQuestions = persistAIQuestions(aiDTOs, targetBankId, teacher);
            log.info("[ExamService][aiGenerateExam] Persisted {} AI questions to bank {}",
                    aiQuestions.size(), targetBankId);
        }

        // ----------------------------------------------------------------
        // STEP 6: Tạo Exam entity + gắn câu hỏi
        // ----------------------------------------------------------------
        Exam exam = buildExam(request, teacher);
        exam = examRepository.save(exam);

        List<ExamQuestion> examQList = buildExamQuestions(exam, selectedFromBank, aiQuestions);
        examQuestionRepository.saveAll(examQList);

        // Cập nhật total_questions
        exam.setTotalQuestions(examQList.size());
        exam.setUpdatedAt(LocalDateTime.now());
        exam = examRepository.save(exam);

        log.info("[ExamService][aiGenerateExam] Exam #{} created: {} from bank, {} from AI",
                exam.getId(), selectedFromBank.size(), aiQuestions.size());

        // ----------------------------------------------------------------
        // STEP 7: Trả về DTO đầy đủ
        // ----------------------------------------------------------------
        return toDetailDTO(exam);
    }

    // =========================================================================
    // PUBLISH / DELETE
    // =========================================================================

    /**
     * Publish đề thi (DRAFT → PUBLISHED).
     */
    @Transactional
    public ExamDTO publishExam(Long examId, User teacher) {
        Exam exam = findExamOrThrow(examId);
        assertOwner(exam, teacher);
        exam.setStatus(Exam.ExamStatus.PUBLISHED);
        exam.setUpdatedAt(LocalDateTime.now());
        return toSummaryDTO(examRepository.save(exam));
    }

    /**
     * Xóa đề thi (cascade xóa exam_questions).
     */
    @Transactional
    public void deleteExam(Long examId, User teacher) {
        Exam exam = findExamOrThrow(examId);
        assertOwner(exam, teacher);
        examRepository.delete(exam);
    }

    // =========================================================================
    // Private helpers – Bank fetching
    // =========================================================================

    /**
     * Lấy câu hỏi từ ngân hàng theo các bộ lọc trong request.
     *
     * <p>Chỉ lấy câu đã được duyệt ({@code isApproved = true}).
     * Nếu {@code bankIds} rỗng/null, trả về danh sách rỗng (để AI sinh toàn bộ).
     */
    private List<Question> fetchFromBank(AIGenerateExamRequest request) {
        if (request.getBankIds() == null || request.getBankIds().isEmpty()) {
            log.info("[ExamService] No bankIds provided – AI will generate all questions");
            return new ArrayList<>();
        }

        // Bulk fetch từ nhiều bank, rồi lọc ở application level
        return questionRepository.findByBankIdIn(request.getBankIds())
                .stream()
                .filter(q -> q.getIsApproved() != null && q.getIsApproved())
                .filter(q -> matchesDifficulty(q, request.getDifficulty()))
                .filter(q -> matchesType(q, request.getQuestionType()))
                .filter(q -> matchesTopic(q, request.getTopic()))
                .toList();
    }

    private boolean matchesDifficulty(Question q, Question.Difficulty difficulty) {
        if (difficulty == null) return true;
        return difficulty.equals(q.getDifficulty());
    }

    private boolean matchesType(Question q, Question.QuestionType type) {
        if (type == null) return true;
        return type.equals(q.getType());
    }

    private boolean matchesTopic(Question q, String topic) {
        if (topic == null || topic.isBlank()) return true;
        if (q.getTopic() == null) return false;
        return q.getTopic().toLowerCase().contains(topic.toLowerCase());
    }

    // =========================================================================
    // Private helpers – AI question persistence
    // =========================================================================

    /**
     * Lưu danh sách câu hỏi AI-generated vào ngân hàng câu hỏi.
     *
     * <p>Nếu {@code targetBankId} là null hoặc không tìm thấy, các câu AI
     * vẫn được tạo dưới dạng entity nhưng KHÔNG liên kết bank (bank sẽ là null).
     * Điều này cho phép đề thi vẫn hoạt động dù không có bank.
     *
     * @param aiDTOs       Danh sách QuestionDTO từ Gemini
     * @param targetBankId ID ngân hàng muốn lưu (nullable)
     * @param teacher      Giáo viên tạo
     * @return Danh sách Question entity đã lưu
     */
    private List<Question> persistAIQuestions(
            List<QuestionDTO> aiDTOs,
            Integer targetBankId,
            User teacher) {

        if (aiDTOs == null || aiDTOs.isEmpty()) {
            return new ArrayList<>();
        }

        // Tìm bank nếu có – dùng QuestionBankRepository trực tiếp
        QuestionBank bank = null;
        if (targetBankId != null) {
            bank = questionBankRepository.findById(targetBankId).orElse(null);
            if (bank == null) {
                log.warn("[ExamService] Bank {} not found – AI questions will not be linked to a bank",
                        targetBankId);
            }
        }

        final QuestionBank finalBank = bank;

        // Nếu không tìm được bank, vẫn tạo câu nhưng cần bank (NOT NULL constraint)
        // => Chỉ lưu khi có bank hợp lệ
        if (finalBank == null) {
            log.info("[ExamService] No valid bank found – AI questions will be created in-memory only (not persisted to bank)");
            return aiDTOs.stream()
                    .filter(dto -> dto != null && dto.getContent() != null && !dto.getContent().isBlank())
                    .map(dto -> buildTransientQuestion(dto, teacher))
                    .toList();
        }

        List<Question> toSave = aiDTOs.stream()
                .filter(dto -> dto != null && dto.getContent() != null && !dto.getContent().isBlank())
                .map(dto -> buildPersistableQuestion(dto, finalBank, teacher))
                .collect(java.util.stream.Collectors.toList());

        return questionRepository.saveAll(toSave);
    }

    private Question buildTransientQuestion(QuestionDTO dto, User teacher) {
        Question.QuestionType type = parseQuestionType(dto.getType());
        Question.Difficulty difficulty = parseDifficulty(dto.getDifficulty());
        return Question.builder()
                .content(dto.getContent())
                .type(type != null ? type : Question.QuestionType.MULTIPLE_CHOICE)
                .difficulty(difficulty != null ? difficulty : Question.Difficulty.MEDIUM)
                .topic(dto.getTopic())
                .options(dto.getOptions())
                .correctAnswer(dto.getCorrectAnswer())
                .explanation(dto.getExplanation())
                .aiGenerated(true)
                .isApproved(false)
                .createdBy(teacher)
                .build();
    }

    private Question buildPersistableQuestion(
            QuestionDTO dto,
            com.planbookai.backend.model.entity.QuestionBank bank,
            User teacher) {
        Question q = buildTransientQuestion(dto, teacher);
        q.setBank(bank);
        return q;
    }

    // =========================================================================
    // Private helpers – Entity building
    // =========================================================================

    private Exam buildExam(AIGenerateExamRequest request, User teacher) {
        return Exam.builder()
                .teacher(teacher)
                .title(request.getTitle())
                .subject(request.getSubject())
                .gradeLevel(request.getGradeLevel())
                .topic(request.getTopic())
                .durationMins(request.getDurationMins())
                .randomized(request.isRandomized())
                .aiGenerated(true)
                .status(Exam.ExamStatus.DRAFT)
                .build();
    }

    /**
     * Xây dựng danh sách ExamQuestion (junction entities).
     *
     * <p>Câu từ bank đứng trước, câu AI theo sau.
     * Nếu {@code randomized=true} trong đề, thứ tự được xáo ngẫu nhiên ở bước trước.
     *
     * @param exam          Đề thi đã được lưu (có ID)
     * @param bankQuestions Câu hỏi lấy từ bank
     * @param aiQuestions   Câu hỏi AI sinh thêm
     * @return Danh sách ExamQuestion theo thứ tự
     */
    private List<ExamQuestion> buildExamQuestions(
            Exam exam,
            List<Question> bankQuestions,
            List<Question> aiQuestions) {

        List<ExamQuestion> result = new ArrayList<>();
        int idx = 1;

        for (Question q : bankQuestions) {
            result.add(ExamQuestion.builder()
                    .exam(exam)
                    .question(q)
                    .orderIndex(idx++)
                    .versionNumber(1)
                    .points(BigDecimal.ONE)
                    .build());
        }

        for (Question q : aiQuestions) {
            result.add(ExamQuestion.builder()
                    .exam(exam)
                    .question(q)
                    .orderIndex(idx++)
                    .versionNumber(1)
                    .points(BigDecimal.ONE)
                    .build());
        }

        return result;
    }

    // =========================================================================
    // Private helpers – DTO mapping
    // =========================================================================

    /**
     * Chuyển Exam → ExamDTO không kèm câu hỏi (dùng cho danh sách).
     */
    private ExamDTO toSummaryDTO(Exam exam) {
        return ExamDTO.builder()
                .id(exam.getId())
                .teacherId(exam.getTeacher() != null ? exam.getTeacher().getId() : null)
                .teacherName(exam.getTeacher() != null ? exam.getTeacher().getFullName() : null)
                .title(exam.getTitle())
                .subject(exam.getSubject())
                .gradeLevel(exam.getGradeLevel())
                .topic(exam.getTopic())
                .totalQuestions(exam.getTotalQuestions())
                .durationMins(exam.getDurationMins())
                .randomized(exam.getRandomized())
                .versionCount(exam.getVersionCount())
                .status(exam.getStatus() != null ? exam.getStatus().name() : null)
                .aiGenerated(exam.getAiGenerated())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển Exam → ExamDTO đầy đủ kèm danh sách câu hỏi.
     */
    private ExamDTO toDetailDTO(Exam exam) {
        ExamDTO dto = toSummaryDTO(exam);

        List<ExamQuestion> examQs = examQuestionRepository.findByExamIdOrderByOrderIndex(exam.getId());
        List<ExamDTO.ExamQuestionDTO> qDTOs = examQs.stream()
                .map(this::toExamQuestionDTO)
                .toList();

        dto.setQuestions(qDTOs);
        return dto;
    }

    private ExamDTO.ExamQuestionDTO toExamQuestionDTO(ExamQuestion eq) {
        String source = Boolean.TRUE.equals(
                eq.getQuestion() != null ? eq.getQuestion().getAiGenerated() : false)
                ? "AI" : "BANK";

        QuestionDTO questionDTO = eq.getQuestion() != null
                ? questionService.mapToQuestionDTO(eq.getQuestion())
                : null;

        return ExamDTO.ExamQuestionDTO.builder()
                .examQuestionId(eq.getId())
                .orderIndex(eq.getOrderIndex())
                .versionNumber(eq.getVersionNumber())
                .points(eq.getPoints())
                .source(source)
                .question(questionDTO)
                .build();
    }

    // =========================================================================
    // Private helpers – lookup / validation
    // =========================================================================

    private Exam findExamOrThrow(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
    }

    private void assertOwner(Exam exam, User user) {
        if (exam.getTeacher() == null || !exam.getTeacher().getId().equals(user.getId())) {
            // Admin/Manager có thể xem
            if (user.getRole() != null &&
                    (user.getRole().getName() == Role.RoleName.ADMIN
                            || user.getRole().getName() == Role.RoleName.MANAGER)) {
                return;
            }
            throw new ForbiddenOperationException("You do not have access to this exam");
        }
    }

    private Exam.ExamStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return Exam.ExamStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Question.QuestionType parseQuestionType(String type) {
        if (type == null) return null;
        try {
            return Question.QuestionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Question.Difficulty parseDifficulty(String difficulty) {
        if (difficulty == null) return null;
        try {
            return Question.Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
