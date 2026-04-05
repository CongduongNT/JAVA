package com.planbookai.backend.service;

import com.planbookai.backend.dto.AIGenerateQuestionsRequest;
import com.planbookai.backend.dto.QuestionBankRequest;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.model.entity.Question;
import com.planbookai.backend.model.entity.QuestionBank;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.QuestionBankRepository;
import com.planbookai.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * QuestionService – Business logic cho Question Bank &amp; Question.
 *
 * <p>Chức năng chính:
 * <ol>
 *   <li>CRUD ngân hàng câu hỏi (QuestionBank).</li>
 *   <li>CRUD câu hỏi thủ công (Question).</li>
 *   <li>Sinh câu hỏi bằng AI (delegate sang GeminiAIService) – có 2 mode:
 *       <ul>
 *         <li>Preview (saveToDb=false): chỉ trả về danh sách, không lưu.</li>
 *         <li>Save (saveToDb=true): lưu vào DB và trả về danh sách đã lưu.</li>
 *       </ul>
 *   </li>
 * </ol>
 */
@Service
public class QuestionService {

    private final QuestionBankRepository bankRepository;
    private final QuestionRepository questionRepository;
    private final GeminiAIService geminiAIService;

    public QuestionService(QuestionBankRepository bankRepository,
                           QuestionRepository questionRepository,
                           GeminiAIService geminiAIService) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
        this.geminiAIService = geminiAIService;
    }

    // =====================================================================
    // QUESTION BANK – CRUD
    // =====================================================================

    /** Lấy danh sách ngân hàng câu hỏi của người dùng hiện tại. */
    public List<QuestionDTO.QuestionBankDTO> getMyBanks(User user) {
        return bankRepository.findByCreatedById(user.getId()).stream()
                .map(this::mapToBankDTO)
                .toList();
    }

    /** Lấy tất cả ngân hàng (admin/manager). */
    public List<QuestionDTO.QuestionBankDTO> getAllBanks() {
        return bankRepository.findAll().stream()
                .map(this::mapToBankDTO)
                .toList();
    }

    /** Tạo ngân hàng câu hỏi mới. */
    public QuestionDTO.QuestionBankDTO createBank(QuestionBankRequest request, User user) {
        QuestionBank bank = QuestionBank.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .gradeLevel(request.getGradeLevel())
                .description(request.getDescription())
                .createdBy(user)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : false)
                .build();
        return mapToBankDTO(bankRepository.save(bank));
    }

    /** Cập nhật ngân hàng câu hỏi. */
    public QuestionDTO.QuestionBankDTO updateBank(Integer id, QuestionBankRequest request) {
        QuestionBank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question bank not found: " + id));
        bank.setName(request.getName());
        bank.setSubject(request.getSubject());
        bank.setGradeLevel(request.getGradeLevel());
        bank.setDescription(request.getDescription());
        if (request.getIsPublished() != null) bank.setIsPublished(request.getIsPublished());
        return mapToBankDTO(bankRepository.save(bank));
    }

    /** Xóa ngân hàng câu hỏi. */
    public void deleteBank(Integer id) {
        bankRepository.deleteById(id);
    }

    // =====================================================================
    // QUESTIONS – CRUD
    // =====================================================================

    /** Lấy danh sách câu hỏi trong 1 ngân hàng. */
    public List<QuestionDTO> getQuestionsByBank(Integer bankId) {
        return questionRepository.findByBankId(bankId).stream()
                .map(this::mapToQuestionDTO)
                .toList();
    }

    /**
     * Lấy danh sách câu hỏi lọc theo trạng thái phê duyệt.
     *
     * @param approved null = tất cả, true = đã duyệt, false = chờ duyệt
     * @return Danh sách QuestionDTO
     */
    public List<QuestionDTO> getQuestionsByApprovalStatus(Boolean approved) {
        List<Question> questions;
        if (approved == null) {
            questions = questionRepository.findAll();
        } else {
            questions = questionRepository.findByIsApproved(approved);
        }
        return questions.stream().map(this::mapToQuestionDTO).toList();
    }

    /** Lấy chi tiết 1 câu hỏi. */
    public QuestionDTO getQuestionById(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
        return mapToQuestionDTO(q);
    }

    /** Xóa câu hỏi. */
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // =====================================================================
    // AI GENERATION
    // =====================================================================

    /**
     * Sinh câu hỏi bằng Gemini AI.
     *
     * <p>Nếu {@code request.isSaveToDb() == false}: chỉ trả về preview, không lưu DB.
     * Nếu {@code request.isSaveToDb() == true}: lưu vào DB rồi trả về danh sách đã lưu.
     *
     * @param request Tham số sinh câu hỏi (bank, subject, topic, difficulty, type, count)
     * @param user    Người dùng đang đăng nhập (Teacher)
     * @return Danh sách QuestionDTO (có id nếu đã lưu, null nếu chỉ preview)
     */
    @Transactional
    public List<QuestionDTO> aiGenerateQuestions(AIGenerateQuestionsRequest request, User user) {
        // 1. Validate bank exists
        QuestionBank bank = bankRepository.findById(request.getBankId())
                .orElseThrow(() -> new RuntimeException("Question bank not found: " + request.getBankId()));

        // 2. Call Gemini AI (sinh câu hỏi, chưa lưu DB)
        List<QuestionDTO> generated = geminiAIService.generateQuestions(
                request.getSubject(),
                request.getTopic(),
                request.getDifficulty(),
                request.getType(),
                request.getCount());

        // 3. Nếu chỉ preview → trả về luôn
        if (!request.isSaveToDb()) {
            // Gắn thêm bankId và bankName vào preview
            generated.forEach(dto -> {
                dto.setBankId(bank.getId());
                dto.setBankName(bank.getName());
            });
            return generated;
        }

        // 4. Lưu vào DB
        List<Question> toSave = generated.stream()
                .map(dto -> buildQuestionEntity(dto, bank, user))
                .toList();

        List<Question> saved = questionRepository.saveAll(toSave);
        return saved.stream().map(this::mapToQuestionDTO).toList();
    }

    /**
     * Lưu danh sách câu hỏi đã được preview (FE gửi lại sau khi chỉnh sửa).
     *
     * @param bankId    ID ngân hàng cần lưu vào
     * @param questions Danh sách câu hỏi đã điều chỉnh từ FE
     * @param user      Người dùng tạo
     * @return Danh sách QuestionDTO đã lưu (có id)
     */
    @Transactional
    public List<QuestionDTO> savePreviewedQuestions(Integer bankId, List<QuestionDTO> questions, User user) {
        QuestionBank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new RuntimeException("Question bank not found: " + bankId));

        List<Question> toSave = questions.stream()
                .map(dto -> buildQuestionEntity(dto, bank, user))
                .toList();

        List<Question> saved = questionRepository.saveAll(toSave);
        return saved.stream().map(this::mapToQuestionDTO).toList();
    }

    // =====================================================================
    // MAPPERS
    // =====================================================================

    private QuestionDTO.QuestionBankDTO mapToBankDTO(QuestionBank bank) {
        return QuestionDTO.QuestionBankDTO.builder()
                .id(bank.getId())
                .name(bank.getName())
                .subject(bank.getSubject())
                .gradeLevel(bank.getGradeLevel())
                .description(bank.getDescription())
                .createdById(bank.getCreatedBy() != null ? bank.getCreatedBy().getId() : null)
                .createdByName(bank.getCreatedBy() != null ? bank.getCreatedBy().getFullName() : null)
                .isPublished(bank.getIsPublished())
                .createdAt(bank.getCreatedAt())
                .build();
    }

    public QuestionDTO mapToQuestionDTO(Question q) {
        return QuestionDTO.builder()
                .id(q.getId())
                .bankId(q.getBank() != null ? q.getBank().getId() : null)
                .bankName(q.getBank() != null ? q.getBank().getName() : null)
                .createdById(q.getCreatedBy() != null ? q.getCreatedBy().getId() : null)
                .createdByName(q.getCreatedBy() != null ? q.getCreatedBy().getFullName() : null)
                .content(q.getContent())
                .type(q.getType() != null ? q.getType().name() : null)
                .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : null)
                .topic(q.getTopic())
                .options(q.getOptions())
                .correctAnswer(q.getCorrectAnswer())
                .explanation(q.getExplanation())
                .aiGenerated(q.getAiGenerated())
                .isApproved(q.getIsApproved())
                .approvedById(q.getApprovedBy() != null ? q.getApprovedBy().getId() : null)
                .approvedByName(q.getApprovedBy() != null ? q.getApprovedBy().getFullName() : null)
                .createdAt(q.getCreatedAt())
                .build();
    }

    private Question buildQuestionEntity(QuestionDTO dto, QuestionBank bank, User user) {
        return Question.builder()
                .bank(bank)
                .createdBy(user)
                .content(dto.getContent())
                .type(Question.QuestionType.valueOf(dto.getType()))
                .difficulty(Question.Difficulty.valueOf(dto.getDifficulty()))
                .topic(dto.getTopic())
                .options(dto.getOptions())
                .correctAnswer(dto.getCorrectAnswer())
                .explanation(dto.getExplanation())
                .aiGenerated(dto.getAiGenerated() != null ? dto.getAiGenerated() : false)
                .isApproved(false)
                .build();
    }

    // =====================================================================
    // APPROVAL
    // =====================================================================

    /**
     * Duyệt hoặc huỷ duyệt một câu hỏi.
     *
     * <p>Khi approve: đặt {@code isApproved = true} và lưu lại ngườị duyệt.
     * <p>Khi unapprove: đặt {@code isApproved = false} và xóa {@code approvedBy}.
     *
     * @param questionId ID câu hỏi cần duyệt
     * @param approve    true = duyệt, false = hủy duyệt
     * @param manager    Người thực hiện duyệt (Manager)
     * @return QuestionDTO sau khi cập nhật
     */
    @Transactional
    public QuestionDTO approveQuestion(Long questionId, boolean approve, User manager) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

        question.setIsApproved(approve);
        question.setApprovedBy(approve ? manager : null);

        Question saved = questionRepository.save(question);
        return mapToQuestionDTO(saved);
    }
}
