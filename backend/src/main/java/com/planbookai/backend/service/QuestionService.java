package com.planbookai.backend.service;

import com.planbookai.backend.dto.AIGenerateQuestionsRequest;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.dto.QuestionBankRequest;
import com.planbookai.backend.dto.QuestionCreateRequest;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.dto.QuestionUpdateRequest;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.Question;
import com.planbookai.backend.model.entity.QuestionBank;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.QuestionBankRepository;
import com.planbookai.backend.repository.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class QuestionService {

    private final QuestionBankRepository bankRepository;
    private final QuestionRepository questionRepository;
    private final GeminiAIService geminiAIService;

    public QuestionService(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository,
            GeminiAIService geminiAIService) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
        this.geminiAIService = geminiAIService;
    }

    public List<QuestionDTO.QuestionBankDTO> getMyBanks(User user) {
        if (hasRole(user, Role.RoleName.ADMIN) || hasRole(user, Role.RoleName.MANAGER)) {
            return getAllBanks();
        }
        return bankRepository.findByCreatedById(user.getId()).stream()
                .map(this::mapToBankDTO)
                .toList();
    }

    public QuestionDTO.QuestionBankDTO getBank(Integer id, User user) {
        QuestionBank bank = findBankOrThrow(id);
        assertCanReadBank(bank, user);
        return mapToBankDTO(bank);
    }

    public List<QuestionDTO.QuestionBankDTO> getAllBanks() {
        return bankRepository.findAll().stream()
                .map(this::mapToBankDTO)
                .toList();
    }

    @Transactional
    public QuestionDTO.QuestionBankDTO createBank(QuestionBankRequest request, User user) {
        assertCanCreateBank(user);

        QuestionBank bank = new QuestionBank();
        bank.setName(request.getName());
        bank.setSubject(request.getSubject());
        bank.setGradeLevel(request.getGradeLevel());
        bank.setDescription(request.getDescription());
        bank.setCreatedBy(user);
        bank.setIsPublished(Boolean.TRUE.equals(request.getIsPublished()));

        return mapToBankDTO(bankRepository.save(bank));
    }

    @Transactional
    public QuestionDTO.QuestionBankDTO updateBank(Integer id, QuestionBankRequest request, User user) {
        QuestionBank bank = findBankOrThrow(id);
        assertCanManageBank(bank, user);

        bank.setName(request.getName());
        bank.setSubject(request.getSubject());
        bank.setGradeLevel(request.getGradeLevel());
        bank.setDescription(request.getDescription());
        if (request.getIsPublished() != null) {
            bank.setIsPublished(request.getIsPublished());
        }

        return mapToBankDTO(bankRepository.save(bank));
    }

    @Transactional
    public void deleteBank(Integer id, User user) {
        QuestionBank bank = findBankOrThrow(id);
        assertCanManageBank(bank, user);
        bankRepository.delete(bank);
    }

    public PageResponse<QuestionDTO> getQuestionsByBank(
            Integer bankId,
            User user,
            Integer page,
            Integer size,
            String topic,
            String difficulty,
            String type) {
        QuestionBank bank = findBankOrThrow(bankId);
        assertCanReadBank(bank, user);

        validatePageRequest(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var questionsPage = questionRepository.findByBankIdWithFilters(
                bankId,
                normalizeFilter(topic),
                parseDifficulty(difficulty),
                parseQuestionType(type),
                pageable);

        return PageResponse.from(questionsPage.map(this::mapToQuestionDTO));
    }

    @Transactional
    public QuestionDTO createQuestion(QuestionCreateRequest request, User user) {
        QuestionBank bank = findBankOrThrow(request.getBankId());
        assertCanManageBank(bank, user);

        Question question = new Question();
        question.setBank(bank);
        question.setCreatedBy(user);
        question.setAiGenerated(false);
        question.setIsApproved(false);
        applyCreateRequest(question, request);

        return mapToQuestionDTO(questionRepository.save(question));
    }

    public QuestionDTO getQuestionById(Long id, User user) {
        Question question = findQuestionOrThrow(id);
        assertCanReadQuestion(question, user);
        return mapToQuestionDTO(question);
    }

    @Transactional
    public QuestionDTO updateQuestion(Long id, QuestionUpdateRequest request, User user) {
        Question question = findQuestionOrThrow(id);
        assertCanManageQuestion(question, user);

        question.setContent(request.getContent());
        question.setType(request.getType());

        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        if (request.getTopic() != null) {
            question.setTopic(request.getTopic());
        }
        if (request.getOptions() != null || request.getType() != Question.QuestionType.MULTIPLE_CHOICE) {
            question.setOptions(request.getType() == Question.QuestionType.MULTIPLE_CHOICE ? request.getOptions() : null);
        }
        if (request.getCorrectAnswer() != null) {
            question.setCorrectAnswer(request.getCorrectAnswer());
        }
        if (request.getExplanation() != null) {
            question.setExplanation(request.getExplanation());
        }

        return mapToQuestionDTO(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id, User user) {
        Question question = findQuestionOrThrow(id);
        assertCanManageQuestion(question, user);
        questionRepository.delete(question);
    }

    @Transactional
    public List<QuestionDTO> aiGenerateQuestions(AIGenerateQuestionsRequest request, User user) {
        QuestionBank bank = findBankOrThrow(request.getBankId());
        assertCanManageBank(bank, user);

        List<QuestionDTO> generated = geminiAIService.generateQuestions(
                request.getSubject(),
                request.getTopic(),
                request.getDifficulty(),
                request.getType(),
                request.getCount());

        if (!request.isSaveToDb()) {
            generated.forEach(dto -> {
                dto.setBankId(bank.getId());
                dto.setBankName(bank.getName());
            });
            return generated;
        }

        List<Question> toSave = generated.stream()
                .map(dto -> buildQuestionEntity(dto, bank, user))
                .toList();

        List<Question> saved = questionRepository.saveAll(toSave);
        return saved.stream().map(this::mapToQuestionDTO).toList();
    }

    @Transactional
    public List<QuestionDTO> savePreviewedQuestions(Integer bankId, List<QuestionDTO> questions, User user) {
        validatePreviewSaveRequest(bankId, questions);

        QuestionBank bank = findBankOrThrow(bankId);
        assertCanManageBank(bank, user);

        List<Question> toSave = questions.stream()
                .map(dto -> buildQuestionEntity(dto, bank, user))
                .toList();

        List<Question> saved = questionRepository.saveAll(toSave);
        return saved.stream().map(this::mapToQuestionDTO).toList();
    }

    private QuestionBank findBankOrThrow(Integer id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question bank not found: " + id));
    }

    private Question findQuestionOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    private void validatePageRequest(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size == null || size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }

    private void validatePreviewSaveRequest(Integer bankId, List<QuestionDTO> questions) {
        if (bankId == null) {
            throw new IllegalArgumentException("bankId is required");
        }
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("questions must not be empty");
        }
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Question.Difficulty parseDifficulty(String value) {
        return parseEnum(value, Question.Difficulty.class, "difficulty");
    }

    private Question.QuestionType parseQuestionType(String value) {
        return parseEnum(value, Question.QuestionType.class, "type");
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String fieldName) {
        String normalized = normalizeFilter(value);
        if (normalized == null) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    private <E extends Enum<E>> E parseRequiredEnum(String value, Class<E> enumType, String fieldName) {
        E parsed = parseEnum(value, enumType, fieldName);
        if (parsed == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return parsed;
    }

    private void assertCanCreateBank(User user) {
        if (hasRole(user, Role.RoleName.TEACHER) || hasRole(user, Role.RoleName.STAFF)) {
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to create question banks");
    }

    private void assertCanManageBank(QuestionBank bank, User user) {
        if (hasRole(user, Role.RoleName.ADMIN)
                || hasRole(user, Role.RoleName.MANAGER)
                || hasRole(user, Role.RoleName.STAFF)
                || (hasRole(user, Role.RoleName.TEACHER) && isOwner(bank, user))) {
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to modify question bank: " + bank.getId());
    }

    private void assertCanReadBank(QuestionBank bank, User user) {
        if (hasRole(user, Role.RoleName.ADMIN)
                || hasRole(user, Role.RoleName.MANAGER)
                || hasRole(user, Role.RoleName.STAFF)
                || isOwner(bank, user)
                || Boolean.TRUE.equals(bank.getIsPublished())) {
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to access question bank: " + bank.getId());
    }

    private void assertCanReadQuestion(Question question, User user) {
        assertCanReadBank(question.getBank(), user);
    }

    private void assertCanManageQuestion(Question question, User user) {
        assertCanManageBank(question.getBank(), user);
    }

    private boolean isOwner(QuestionBank bank, User user) {
        return bank.getCreatedBy() != null
                && user != null
                && bank.getCreatedBy().getId().equals(user.getId());
    }

    private boolean hasRole(User user, Role.RoleName roleName) {
        return user != null
                && user.getRole() != null
                && user.getRole().getName() == roleName;
    }

    private QuestionDTO.QuestionBankDTO mapToBankDTO(QuestionBank bank) {
        QuestionDTO.QuestionBankDTO dto = new QuestionDTO.QuestionBankDTO();
        dto.setId(bank.getId());
        dto.setName(bank.getName());
        dto.setSubject(bank.getSubject());
        dto.setGradeLevel(bank.getGradeLevel());
        dto.setDescription(bank.getDescription());
        dto.setCreatedById(bank.getCreatedBy() != null ? bank.getCreatedBy().getId() : null);
        dto.setCreatedByName(bank.getCreatedBy() != null ? bank.getCreatedBy().getFullName() : null);
        dto.setIsPublished(bank.getIsPublished());
        dto.setCreatedAt(bank.getCreatedAt());
        return dto;
    }

    public QuestionDTO mapToQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setBankId(question.getBank() != null ? question.getBank().getId() : null);
        dto.setBankName(question.getBank() != null ? question.getBank().getName() : null);
        dto.setCreatedById(question.getCreatedBy() != null ? question.getCreatedBy().getId() : null);
        dto.setCreatedByName(question.getCreatedBy() != null ? question.getCreatedBy().getFullName() : null);
        dto.setContent(question.getContent());
        dto.setType(question.getType() != null ? question.getType().name() : null);
        dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);
        dto.setTopic(question.getTopic());
        dto.setOptions(question.getOptions());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setAiGenerated(question.getAiGenerated());
        dto.setIsApproved(question.getIsApproved());
        dto.setCreatedAt(question.getCreatedAt());
        return dto;
    }

    private Question buildQuestionEntity(QuestionDTO dto, QuestionBank bank, User user) {
        if (dto == null) {
            throw new IllegalArgumentException("Question payload must not be null");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new IllegalArgumentException("Question content is required");
        }

        Question.QuestionType questionType = parseRequiredEnum(dto.getType(), Question.QuestionType.class, "type");
        Question.Difficulty questionDifficulty = parseEnum(dto.getDifficulty(), Question.Difficulty.class, "difficulty");

        Question question = new Question();
        question.setBank(bank);
        question.setCreatedBy(user);
        question.setContent(dto.getContent());
        question.setType(questionType);
        question.setDifficulty(questionDifficulty != null ? questionDifficulty : Question.Difficulty.MEDIUM);
        question.setTopic(dto.getTopic());
        question.setOptions(dto.getOptions());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setExplanation(dto.getExplanation());
        question.setAiGenerated(dto.getAiGenerated() != null ? dto.getAiGenerated() : false);
        question.setIsApproved(false);
        return question;
    }

    private void applyCreateRequest(Question question, QuestionCreateRequest request) {
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : Question.Difficulty.MEDIUM);
        question.setTopic(request.getTopic());
        question.setOptions(request.getType() == Question.QuestionType.MULTIPLE_CHOICE ? request.getOptions() : null);
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());
    }
}
