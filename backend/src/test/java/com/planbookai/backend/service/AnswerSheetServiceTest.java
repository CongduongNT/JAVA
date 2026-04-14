package com.planbookai.backend.service;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.UploadAnswerSheetRequest;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import com.planbookai.backend.repository.ExamRepository;
import com.planbookai.backend.util.FileStorageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerSheetServiceTest {

    @Test
    void uploadAnswerSheetsCreatesPendingRowsForOwnedExam() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.exams.put(11L, buildExam(11L, teacher));
        RecordingStorageService storageService = new RecordingStorageService();
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                storageService);

        MockMultipartFile firstFile = new MockMultipartFile(
                "files",
                "sheet-01.png",
                "image/png",
                "first".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile secondFile = new MockMultipartFile(
                "files",
                "sheet-02.png",
                "image/png",
                "second".getBytes(StandardCharsets.UTF_8));
        UploadAnswerSheetRequest request = new UploadAnswerSheetRequest();
        request.setExamId(11L);
        request.setFiles(List.of(firstFile, secondFile));

        List<AnswerSheetDTO> response = answerSheetService.uploadAnswerSheets(request, teacher);

        assertEquals(2, storageService.files.size());
        assertSame(firstFile, storageService.files.get(0));
        assertSame(secondFile, storageService.files.get(1));
        assertEquals("answer-sheets/7/exam-11", storageService.folders.get(0));
        assertEquals("answer-sheets/7/exam-11", storageService.folders.get(1));
        assertEquals(2, state.savedAnswerSheets.size());
        assertEquals(2, response.size());
        assertEquals(11L, response.get(0).getExamId());
        assertEquals(teacher.getId(), response.get(0).getTeacherId());
        assertEquals(AnswerSheet.OcrStatus.PENDING, response.get(0).getOcrStatus());
        assertTrue(response.get(0).getFileUrl().contains("sheet-01.png"));
        assertNotNull(response.get(0).getUploadedAt());
    }

    @Test
    void uploadAnswerSheetsRejectsUnauthenticatedUser() {
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                new RecordingStorageService());

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(10L, List.of(buildFile("sheet.png"))),
                        null));

        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void uploadAnswerSheetsRejectsNonTeacherUser() {
        User staff = buildUser(7L, Role.RoleName.STAFF);
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                new RecordingStorageService());

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(10L, List.of(buildFile("sheet.png"))),
                        staff));

        assertEquals("Only teacher can upload answer sheets", exception.getMessage());
    }

    @Test
    void uploadAnswerSheetsRejectsMissingExamId() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RecordingStorageService storageService = new RecordingStorageService();
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                storageService);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(null, List.of(buildFile("sheet.png"))),
                        teacher));

        assertEquals("examId is required", exception.getMessage());
        assertEquals(0, storageService.files.size());
    }

    @Test
    void uploadAnswerSheetsRejectsMissingFiles() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RecordingStorageService storageService = new RecordingStorageService();
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                storageService);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(11L, List.of()),
                        teacher));

        assertEquals("files is required", exception.getMessage());
        assertEquals(0, storageService.files.size());
    }

    @Test
    void uploadAnswerSheetsRejectsEmptyFileBeforeUpload() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RecordingStorageService storageService = new RecordingStorageService();
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                storageService);

        MockMultipartFile emptyFile = new MockMultipartFile(
                "files",
                "empty.png",
                "image/png",
                new byte[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(11L, List.of(emptyFile)),
                        teacher));

        assertEquals("files must not contain empty file", exception.getMessage());
        assertEquals(0, storageService.files.size());
    }

    @Test
    void uploadAnswerSheetsRejectsUnknownExam() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RecordingStorageService storageService = new RecordingStorageService();
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                storageService);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(404L, List.of(buildFile("sheet.png"))),
                        teacher));

        assertEquals("Exam not found: 404", exception.getMessage());
        assertEquals(0, storageService.files.size());
    }

    @Test
    void uploadAnswerSheetsRejectsExamOwnedByOtherTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        User otherTeacher = buildUser(8L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.exams.put(11L, buildExam(11L, otherTeacher));
        RecordingStorageService storageService = new RecordingStorageService();
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                storageService);

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> answerSheetService.uploadAnswerSheets(
                        new UploadAnswerSheetRequest(11L, List.of(buildFile("sheet.png"))),
                        teacher));

        assertEquals("You do not have permission to upload answer sheets for this exam", exception.getMessage());
        assertEquals(0, storageService.files.size());
        assertEquals(0, state.savedAnswerSheets.size());
    }

    private AnswerSheetRepository createAnswerSheetRepository(RepositoryState state) {
        return (AnswerSheetRepository) Proxy.newProxyInstance(
                AnswerSheetRepository.class.getClassLoader(),
                new Class<?>[]{AnswerSheetRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "saveAll":
                            List<AnswerSheet> saved = new ArrayList<>();
                            for (AnswerSheet answerSheet : (Iterable<AnswerSheet>) args[0]) {
                                if (answerSheet.getId() == null) {
                                    answerSheet.setId(state.nextId++);
                                }
                                if (answerSheet.getUploadedAt() == null) {
                                    answerSheet.setUploadedAt(LocalDateTime.now());
                                }
                                state.savedAnswerSheets.add(answerSheet);
                                state.answerSheets.put(answerSheet.getId(), answerSheet);
                                saved.add(answerSheet);
                            }
                            return saved;
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

    private ExamRepository createExamRepository(RepositoryState state) {
        return (ExamRepository) Proxy.newProxyInstance(
                ExamRepository.class.getClassLoader(),
                new Class<?>[]{ExamRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findById":
                            return Optional.ofNullable(state.exams.get((Long) args[0]));
                        case "toString":
                            return "ExamRepositoryProxy";
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

    private Exam buildExam(Long id, User teacher) {
        return Exam.builder()
                .id(id)
                .teacher(teacher)
                .title("Exam")
                .subject("Chemistry")
                .gradeLevel("10")
                .topic("Atomic")
                .status(Exam.ExamStatus.DRAFT)
                .build();
    }

    private MockMultipartFile buildFile(String filename) {
        return new MockMultipartFile(
                "files",
                filename,
                "image/png",
                "content".getBytes(StandardCharsets.UTF_8));
    }

    private static final class RepositoryState {
        private final Map<Long, Exam> exams = new HashMap<>();
        private final Map<Long, AnswerSheet> answerSheets = new HashMap<>();
        private final List<AnswerSheet> savedAnswerSheets = new ArrayList<>();
        private long nextId = 100L;
    }

    private static final class RecordingStorageService extends StorageService {
        private final List<String> folders = new ArrayList<>();
        private final List<MultipartFile> files = new ArrayList<>();

        private RecordingStorageService() {
            super(new FileStorageUtil(createNoOpS3Client(), "planbookai-storage", "https://demo.supabase.co"));
        }

        @Override
        public String uploadFile(String folder, MultipartFile file) {
            folders.add(folder);
            files.add(file);
            return "https://demo.supabase.co/storage/" + file.getOriginalFilename();
        }

        private static S3Client createNoOpS3Client() {
            return (S3Client) Proxy.newProxyInstance(
                    S3Client.class.getClassLoader(),
                    new Class<?>[]{S3Client.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "serviceName":
                                return "s3";
                            case "close":
                                return null;
                            case "toString":
                                return "S3ClientNoOpProxy";
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            default:
                                throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
