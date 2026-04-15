package com.planbookai.backend.service;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Test
    void getMyAnswerSheetsReturnsCurrentTeacherRowsSortedByUploadedAtDesc() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        User otherTeacher = buildUser(8L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.exams.put(11L, buildExam(11L, teacher));
        state.exams.put(12L, buildExam(12L, teacher));
        state.exams.put(13L, buildExam(13L, otherTeacher));
        state.answerSheets.put(101L, buildAnswerSheet(101L, 11L, teacher, "sheet-01.png", LocalDateTime.now().minusMinutes(20)));
        state.answerSheets.put(102L, buildAnswerSheet(102L, 12L, teacher, "sheet-02.png", LocalDateTime.now().minusMinutes(5)));
        state.answerSheets.put(103L, buildAnswerSheet(103L, 13L, otherTeacher, "sheet-03.png", LocalDateTime.now().minusMinutes(1)));
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                new RecordingStorageService());

        PageResponse<AnswerSheetDTO> response = answerSheetService.getMyAnswerSheets(teacher, 0, 10, null);

        assertEquals(2, response.getContent().size());
        assertEquals(102L, response.getContent().get(0).getId());
        assertEquals(101L, response.getContent().get(1).getId());
        assertEquals(2L, response.getTotalElements());
    }

    @Test
    void getMyAnswerSheetsFiltersByOwnedExam() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.exams.put(11L, buildExam(11L, teacher));
        state.exams.put(12L, buildExam(12L, teacher));
        state.answerSheets.put(101L, buildAnswerSheet(101L, 11L, teacher, "sheet-01.png", LocalDateTime.now().minusMinutes(20)));
        state.answerSheets.put(102L, buildAnswerSheet(102L, 12L, teacher, "sheet-02.png", LocalDateTime.now().minusMinutes(5)));
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                new RecordingStorageService());

        PageResponse<AnswerSheetDTO> response = answerSheetService.getMyAnswerSheets(teacher, 0, 10, 11L);

        assertEquals(1, response.getContent().size());
        assertEquals(101L, response.getContent().get(0).getId());
        assertEquals(11L, response.getContent().get(0).getExamId());
    }

    @Test
    void getMyAnswerSheetsRejectsOtherTeacherExamFilter() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        User otherTeacher = buildUser(8L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.exams.put(99L, buildExam(99L, otherTeacher));
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                new RecordingStorageService());

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> answerSheetService.getMyAnswerSheets(teacher, 0, 10, 99L));

        assertEquals("You do not have permission to access answer sheets for this exam", exception.getMessage());
    }

    @Test
    void getMyAnswerSheetsRejectsInvalidPagination() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                new RecordingStorageService());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> answerSheetService.getMyAnswerSheets(teacher, -1, 10, null));

        assertEquals("page must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    void getAnswerSheetReturnsOwnedSheet() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        AnswerSheet answerSheet = buildAnswerSheet(201L, 11L, teacher, "sheet-01.png", LocalDateTime.now().minusMinutes(2));
        answerSheet.setStudentName("Nguyen Van A");
        answerSheet.setStudentCode("HS001");
        answerSheet.setOcrRawData("{\"student_name\":\"Nguyen Van A\"}");
        answerSheet.setOcrStatus(AnswerSheet.OcrStatus.COMPLETED);
        state.answerSheets.put(201L, answerSheet);
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                new RecordingStorageService());

        AnswerSheetDTO response = answerSheetService.getAnswerSheet(201L, teacher);

        assertEquals(201L, response.getId());
        assertEquals("Nguyen Van A", response.getStudentName());
        assertEquals("HS001", response.getStudentCode());
        assertEquals(AnswerSheet.OcrStatus.COMPLETED, response.getOcrStatus());
    }

    @Test
    void getAnswerSheetRejectsOtherTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        User otherTeacher = buildUser(8L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.answerSheets.put(202L, buildAnswerSheet(202L, 11L, otherTeacher, "sheet-02.png", LocalDateTime.now()));
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(state),
                createExamRepository(state),
                new RecordingStorageService());

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> answerSheetService.getAnswerSheet(202L, teacher));

        assertEquals("You do not have permission to access this answer sheet", exception.getMessage());
    }

    @Test
    void getAnswerSheetThrowsNotFoundWhenMissing() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        AnswerSheetService answerSheetService = new AnswerSheetService(
                createAnswerSheetRepository(new RepositoryState()),
                createExamRepository(new RepositoryState()),
                new RecordingStorageService());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> answerSheetService.getAnswerSheet(404L, teacher));

        assertEquals("Answer sheet not found: 404", exception.getMessage());
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
                        case "findById":
                            return Optional.ofNullable(state.answerSheets.get((Long) args[0]));
                        case "findByTeacher_Id":
                            return buildAnswerSheetPage(
                                    state,
                                    (Long) args[0],
                                    null,
                                    (Pageable) args[1]);
                        case "findByTeacher_IdAndExam_Id":
                            return buildAnswerSheetPage(
                                    state,
                                    (Long) args[0],
                                    (Long) args[1],
                                    (Pageable) args[2]);
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

    private Page<AnswerSheet> buildAnswerSheetPage(
            RepositoryState state,
            Long teacherId,
            Long examId,
            Pageable pageable) {
        List<AnswerSheet> filtered = state.answerSheets.values().stream()
                .filter(answerSheet -> answerSheet.getTeacher() != null
                        && teacherId.equals(answerSheet.getTeacher().getId()))
                .filter(answerSheet -> examId == null
                        || (answerSheet.getExam() != null && examId.equals(answerSheet.getExam().getId())))
                .sorted(Comparator
                        .comparing(AnswerSheet::getUploadedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AnswerSheet::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<AnswerSheet> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(content, pageable, filtered.size());
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

    private AnswerSheet buildAnswerSheet(Long id, Long examId, User teacher, String fileName, LocalDateTime uploadedAt) {
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setId(id);
        answerSheet.setExam(buildExam(examId, teacher));
        answerSheet.setTeacher(teacher);
        answerSheet.setFileUrl("https://demo.supabase.co/storage/" + fileName);
        answerSheet.setOcrStatus(AnswerSheet.OcrStatus.PENDING);
        answerSheet.setUploadedAt(uploadedAt);
        return answerSheet;
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
