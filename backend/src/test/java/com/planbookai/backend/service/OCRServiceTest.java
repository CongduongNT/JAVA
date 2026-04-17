package com.planbookai.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.exception.AIServiceException;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OCRServiceTest {

    @Test
    void processAnswerSheetCompletesAndPersistsOcrDataForOwnedTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        AnswerSheet answerSheet = buildAnswerSheet(21L, teacher, AnswerSheet.OcrStatus.PENDING);
        state.store.put(answerSheet.getId(), answerSheet);
        RecordingAnswerSheetFileLoader fileLoader = new RecordingAnswerSheetFileLoader();
        fileLoader.loadedFile = new AnswerSheetFileLoader.LoadedAnswerSheetFile(
                "image".getBytes(),
                "image/png");
        RecordingGeminiVisionClient geminiVisionClient = new RecordingGeminiVisionClient();
        geminiVisionClient.response = """
                {
                  "student_name": "Nguyen Van A",
                  "student_code": "HS001",
                  "extracted_text": "1. A\\n2. B",
                  "answers": [
                    { "question_number": "1", "answer": "A", "evidence": "1. A" }
                  ],
                  "notes": []
                }
                """;
        OCRService ocrService = new OCRService(
                createAnswerSheetRepository(state),
                fileLoader,
                geminiVisionClient,
                new ObjectMapper());

        AnswerSheetDTO response = ocrService.processAnswerSheet(21L, teacher);

        assertEquals("https://demo.supabase.co/storage/sheet.png", fileLoader.fileUrl);
        assertEquals(1, geminiVisionClient.invocations);
        assertEquals(0, state.findByIdCalls);
        assertEquals(1, state.findByIdForUpdateCalls);
        assertEquals(5, geminiVisionClient.fileContent.length);
        assertEquals("image/png", geminiVisionClient.mimeType);
        assertTrue(geminiVisionClient.prompt.contains("strict JSON"));
        assertEquals(AnswerSheet.OcrStatus.COMPLETED, answerSheet.getOcrStatus());
        assertEquals("Nguyen Van A", answerSheet.getStudentName());
        assertEquals("HS001", answerSheet.getStudentCode());
        assertTrue(answerSheet.getOcrRawData().contains("\"student_name\":\"Nguyen Van A\""));
        assertEquals(AnswerSheet.OcrStatus.COMPLETED, response.getOcrStatus());
        assertEquals("Nguyen Van A", response.getStudentName());
        assertEquals("HS001", response.getStudentCode());
        assertTrue(state.savedStatuses.contains(AnswerSheet.OcrStatus.PROCESSING));
        assertTrue(state.savedStatuses.contains(AnswerSheet.OcrStatus.COMPLETED));
    }

    @Test
    void processAnswerSheetReturnsExistingCompletedResultWithoutCallingVision() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        AnswerSheet answerSheet = buildAnswerSheet(22L, teacher, AnswerSheet.OcrStatus.COMPLETED);
        answerSheet.setStudentName("Existing Student");
        answerSheet.setStudentCode("HS002");
        answerSheet.setOcrRawData("{\"student_name\":\"Existing Student\"}");
        state.store.put(answerSheet.getId(), answerSheet);
        RecordingAnswerSheetFileLoader fileLoader = new RecordingAnswerSheetFileLoader();
        RecordingGeminiVisionClient geminiVisionClient = new RecordingGeminiVisionClient();
        OCRService ocrService = new OCRService(
                createAnswerSheetRepository(state),
                fileLoader,
                geminiVisionClient,
                new ObjectMapper());

        AnswerSheetDTO response = ocrService.processAnswerSheet(22L, teacher);

        assertEquals(0, geminiVisionClient.invocations);
        assertEquals(0, state.savedStatuses.size());
        assertEquals(AnswerSheet.OcrStatus.COMPLETED, response.getOcrStatus());
        assertEquals("Existing Student", response.getStudentName());
    }

    @Test
    void processAnswerSheetRejectsOtherTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        User otherTeacher = buildUser(8L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.store.put(23L, buildAnswerSheet(23L, otherTeacher, AnswerSheet.OcrStatus.PENDING));
        OCRService ocrService = new OCRService(
                createAnswerSheetRepository(state),
                new RecordingAnswerSheetFileLoader(),
                new RecordingGeminiVisionClient(),
                new ObjectMapper());

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> ocrService.processAnswerSheet(23L, teacher));

        assertEquals("You do not have permission to process this answer sheet", exception.getMessage());
    }

    @Test
    void processAnswerSheetRejectsProcessingInProgress() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.store.put(24L, buildAnswerSheet(24L, teacher, AnswerSheet.OcrStatus.PROCESSING));
        OCRService ocrService = new OCRService(
                createAnswerSheetRepository(state),
                new RecordingAnswerSheetFileLoader(),
                new RecordingGeminiVisionClient(),
                new ObjectMapper());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ocrService.processAnswerSheet(24L, teacher));

        assertEquals("Answer sheet is already being processed", exception.getMessage());
    }

    @Test
    void processAnswerSheetMarksFailedWhenGeminiReturnsInvalidJson() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        AnswerSheet answerSheet = buildAnswerSheet(25L, teacher, AnswerSheet.OcrStatus.PENDING);
        state.store.put(answerSheet.getId(), answerSheet);
        RecordingAnswerSheetFileLoader fileLoader = new RecordingAnswerSheetFileLoader();
        fileLoader.loadedFile = new AnswerSheetFileLoader.LoadedAnswerSheetFile(
                "image".getBytes(),
                "image/png");
        RecordingGeminiVisionClient geminiVisionClient = new RecordingGeminiVisionClient();
        geminiVisionClient.response = "not-json";
        OCRService ocrService = new OCRService(
                createAnswerSheetRepository(state),
                fileLoader,
                geminiVisionClient,
                new ObjectMapper());

        AIServiceException exception = assertThrows(
                AIServiceException.class,
                () -> ocrService.processAnswerSheet(25L, teacher));

        assertEquals("Gemini Vision returned invalid OCR JSON", exception.getMessage());
        assertEquals(AnswerSheet.OcrStatus.FAILED, answerSheet.getOcrStatus());
        assertTrue(answerSheet.getOcrRawData().contains("\"error\":\"Gemini Vision returned invalid OCR JSON\""));
        assertTrue(state.savedStatuses.contains(AnswerSheet.OcrStatus.PROCESSING));
        assertTrue(state.savedStatuses.contains(AnswerSheet.OcrStatus.FAILED));
    }

    @Test
    void processAnswerSheetThrowsNotFoundWhenMissing() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        OCRService ocrService = new OCRService(
                createAnswerSheetRepository(new RepositoryState()),
                new RecordingAnswerSheetFileLoader(),
                new RecordingGeminiVisionClient(),
                new ObjectMapper());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ocrService.processAnswerSheet(404L, teacher));

        assertEquals("Answer sheet not found: 404", exception.getMessage());
    }

    private AnswerSheetRepository createAnswerSheetRepository(RepositoryState state) {
        return (AnswerSheetRepository) Proxy.newProxyInstance(
                AnswerSheetRepository.class.getClassLoader(),
                new Class<?>[]{AnswerSheetRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findById":
                            state.findByIdCalls++;
                            return Optional.ofNullable(state.store.get((Long) args[0]));
                        case "findByIdForUpdate":
                            state.findByIdForUpdateCalls++;
                            return Optional.ofNullable(state.store.get((Long) args[0]));
                        case "save":
                            AnswerSheet answerSheet = (AnswerSheet) args[0];
                            if (answerSheet.getId() == null) {
                                answerSheet.setId(state.nextId++);
                            }
                            if (answerSheet.getUploadedAt() == null) {
                                answerSheet.setUploadedAt(LocalDateTime.now());
                            }
                            state.store.put(answerSheet.getId(), answerSheet);
                            state.savedStatuses.add(answerSheet.getOcrStatus());
                            return answerSheet;
                        case "toString":
                            return "AnswerSheetRepositoryProxy";
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private User buildUser(Long id, Role.RoleName roleName) {
        Role role = Role.builder().id(1).name(roleName).build();
        return User.builder()
                .id(id)
                .email("teacher@planbookai.com")
                .fullName("Teacher")
                .passwordHash("encoded")
                .role(role)
                .build();
    }

    private AnswerSheet buildAnswerSheet(Long id, User teacher, AnswerSheet.OcrStatus status) {
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setId(id);
        answerSheet.setTeacher(teacher);
        answerSheet.setExam(Exam.builder().id(12L).teacher(teacher).title("Exam").build());
        answerSheet.setFileUrl("https://demo.supabase.co/storage/sheet.png");
        answerSheet.setOcrStatus(status);
        answerSheet.setUploadedAt(LocalDateTime.now().minusMinutes(5));
        return answerSheet;
    }

    private static final class RepositoryState {
        private final Map<Long, AnswerSheet> store = new HashMap<>();
        private final List<AnswerSheet.OcrStatus> savedStatuses = new ArrayList<>();
        private int findByIdCalls;
        private int findByIdForUpdateCalls;
        private long nextId = 100L;
    }

    private static final class RecordingAnswerSheetFileLoader implements AnswerSheetFileLoader {
        private String fileUrl;
        private LoadedAnswerSheetFile loadedFile;

        @Override
        public LoadedAnswerSheetFile load(String fileUrl) {
            this.fileUrl = fileUrl;
            return loadedFile;
        }
    }

    private static final class RecordingGeminiVisionClient implements GeminiVisionClient {
        private int invocations;
        private byte[] fileContent;
        private String mimeType;
        private String prompt;
        private String response;

        @Override
        public String extractAnswerSheetJson(byte[] fileContent, String mimeType, String prompt) {
            this.invocations++;
            this.fileContent = fileContent;
            this.mimeType = mimeType;
            this.prompt = prompt;
            assertNotNull(fileContent);
            return response;
        }
    }
}
