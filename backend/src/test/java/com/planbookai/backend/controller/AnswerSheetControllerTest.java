package com.planbookai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.dto.UploadAnswerSheetRequest;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import com.planbookai.backend.repository.ExamRepository;
import com.planbookai.backend.service.AnswerSheetFileLoader;
import com.planbookai.backend.service.AnswerSheetService;
import com.planbookai.backend.service.GeminiVisionClient;
import com.planbookai.backend.service.OCRService;
import com.planbookai.backend.service.StorageService;
import com.planbookai.backend.util.FileStorageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnswerSheetControllerTest {

    @Test
    void uploadAnswerSheetsDelegatesToServiceWithSnakeCaseExamId() {
        User teacher = buildTeacher(5L);
        MockMultipartFile file = buildFile("sheet-01.png");
        AnswerSheetDTO dto = buildAnswerSheetDTO(1L, 12L, teacher.getId());
        List<AnswerSheetDTO> expected = List.of(dto);
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.uploadResponse = expected;
        AnswerSheetController controller = new AnswerSheetController(service, new RecordingOCRService());

        ResponseEntity<List<AnswerSheetDTO>> response = controller.uploadAnswerSheets(
                12L,
                null,
                List.of(file),
                teacher);

        assertEquals(201, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertSame(teacher, service.uploadUser);
        assertEquals(12L, service.uploadRequest.getExamId());
        assertSame(file, service.uploadRequest.getFiles().get(0));
    }

    @Test
    void uploadAnswerSheetsDelegatesToServiceWithCamelCaseExamId() {
        User teacher = buildTeacher(5L);
        MockMultipartFile file = buildFile("sheet-01.png");
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.uploadResponse = List.of();
        AnswerSheetController controller = new AnswerSheetController(service, new RecordingOCRService());

        ResponseEntity<List<AnswerSheetDTO>> response = controller.uploadAnswerSheets(
                null,
                15L,
                List.of(file),
                teacher);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(15L, service.uploadRequest.getExamId());
        assertSame(file, service.uploadRequest.getFiles().get(0));
    }

    @Test
    void getAnswerSheetsDelegatesToServiceWithSnakeCaseExamId() {
        User teacher = buildTeacher(5L);
        PageResponse<AnswerSheetDTO> expected = new PageResponse<>(List.of(buildAnswerSheetDTO(1L, 12L, 5L)), 0, 10, 1, 1, true, true, false);
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.listResponse = expected;
        AnswerSheetController controller = new AnswerSheetController(service, new RecordingOCRService());

        ResponseEntity<PageResponse<AnswerSheetDTO>> response = controller.getAnswerSheets(
                0,
                10,
                12L,
                null,
                teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertSame(teacher, service.listUser);
        assertEquals(0, service.listPage);
        assertEquals(10, service.listSize);
        assertEquals(12L, service.listExamId);
    }

    @Test
    void getAnswerSheetsDelegatesToServiceWithoutExamFilter() {
        User teacher = buildTeacher(5L);
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.listResponse = new PageResponse<>(List.of(), 0, 10, 0, 0, true, true, true);
        AnswerSheetController controller = new AnswerSheetController(service, new RecordingOCRService());

        ResponseEntity<PageResponse<AnswerSheetDTO>> response = controller.getAnswerSheets(
                1,
                20,
                null,
                null,
                teacher);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, service.listPage);
        assertEquals(20, service.listSize);
        assertEquals(null, service.listExamId);
    }

    @Test
    void getAnswerSheetDelegatesToService() {
        User teacher = buildTeacher(5L);
        AnswerSheetDTO expected = buildAnswerSheetDTO(21L, 12L, 5L);
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.detailResponse = expected;
        AnswerSheetController controller = new AnswerSheetController(service, new RecordingOCRService());

        ResponseEntity<AnswerSheetDTO> response = controller.getAnswerSheet(21L, teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(21L, service.detailId);
        assertSame(teacher, service.detailUser);
    }

    @Test
    void processAnswerSheetDelegatesToOcrService() {
        User teacher = buildTeacher(5L);
        AnswerSheetDTO expected = buildAnswerSheetDTO(30L, 12L, 5L);
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        RecordingOCRService ocrService = new RecordingOCRService();
        ocrService.response = expected;
        AnswerSheetController controller = new AnswerSheetController(service, ocrService);

        ResponseEntity<AnswerSheetDTO> response = controller.processAnswerSheet(30L, teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(30L, ocrService.answerSheetId);
        assertSame(teacher, ocrService.user);
    }

    @Test
    void uploadAnswerSheetsRejectsAmbiguousExamId() {
        AnswerSheetController controller = new AnswerSheetController(
                new RecordingAnswerSheetService(),
                new RecordingOCRService());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadAnswerSheets(10L, 11L, List.of(buildFile("sheet-01.png")), buildTeacher(5L)));

        assertEquals("Provide either exam_id or examId with the same value", exception.getMessage());
    }

    @Test
    void getAnswerSheetsRejectsAmbiguousExamId() {
        AnswerSheetController controller = new AnswerSheetController(
                new RecordingAnswerSheetService(),
                new RecordingOCRService());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.getAnswerSheets(0, 10, 10L, 11L, buildTeacher(5L)));

        assertEquals("Provide either exam_id or examId with the same value", exception.getMessage());
    }

    @Test
    void answerSheetEndpointsRequireTeacherRole() throws NoSuchMethodException {
        assertTeacherRole("uploadAnswerSheets", Long.class, Long.class, List.class, User.class);
        assertTeacherRole("getAnswerSheets", Integer.class, Integer.class, Long.class, Long.class, User.class);
        assertTeacherRole("getAnswerSheet", Long.class, User.class);
        assertTeacherRole("processAnswerSheet", Long.class, User.class);
    }

    private void assertTeacherRole(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = AnswerSheetController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals("hasRole('TEACHER')", preAuthorize.value());
    }

    private User buildTeacher(Long id) {
        return User.builder()
                .id(id)
                .email("teacher@planbookai.com")
                .fullName("Teacher")
                .passwordHash("encoded")
                .role(Role.builder().id(1).name(Role.RoleName.TEACHER).build())
                .build();
    }

    private AnswerSheetDTO buildAnswerSheetDTO(Long id, Long examId, Long teacherId) {
        AnswerSheetDTO dto = new AnswerSheetDTO();
        dto.setId(id);
        dto.setExamId(examId);
        dto.setTeacherId(teacherId);
        dto.setFileUrl("https://demo.supabase.co/storage/sheet-" + id + ".png");
        dto.setOcrStatus(AnswerSheet.OcrStatus.PENDING);
        dto.setUploadedAt(LocalDateTime.now());
        return dto;
    }

    private MockMultipartFile buildFile(String filename) {
        return new MockMultipartFile("files", filename, "image/png", "content".getBytes());
    }

    private static final class RecordingAnswerSheetService extends AnswerSheetService {
        private UploadAnswerSheetRequest uploadRequest;
        private User uploadUser;
        private List<AnswerSheetDTO> uploadResponse;
        private Integer listPage;
        private Integer listSize;
        private Long listExamId;
        private User listUser;
        private PageResponse<AnswerSheetDTO> listResponse;
        private Long detailId;
        private User detailUser;
        private AnswerSheetDTO detailResponse;

        private RecordingAnswerSheetService() {
            super(createNoOpAnswerSheetRepository(), createNoOpExamRepository(), createNoOpStorageService());
        }

        @Override
        public List<AnswerSheetDTO> uploadAnswerSheets(UploadAnswerSheetRequest request, User user) {
            this.uploadRequest = request;
            this.uploadUser = user;
            return uploadResponse;
        }

        @Override
        public PageResponse<AnswerSheetDTO> getMyAnswerSheets(User user, Integer page, Integer size, Long examId) {
            this.listUser = user;
            this.listPage = page;
            this.listSize = size;
            this.listExamId = examId;
            return listResponse;
        }

        @Override
        public AnswerSheetDTO getAnswerSheet(Long id, User user) {
            this.detailId = id;
            this.detailUser = user;
            return detailResponse;
        }
    }

    private static final class RecordingOCRService extends OCRService {
        private Long answerSheetId;
        private User user;
        private AnswerSheetDTO response;

        private RecordingOCRService() {
            super(
                    createNoOpAnswerSheetRepository(),
                    fileUrl -> new AnswerSheetFileLoader.LoadedAnswerSheetFile("file".getBytes(), "image/png"),
                    new NoOpGeminiVisionClient(),
                    new ObjectMapper());
        }

        @Override
        public AnswerSheetDTO processAnswerSheet(Long answerSheetId, User user) {
            this.answerSheetId = answerSheetId;
            this.user = user;
            return response;
        }
    }

    private static final class NoOpGeminiVisionClient implements GeminiVisionClient {
        @Override
        public String extractAnswerSheetJson(byte[] fileContent, String mimeType, String prompt) {
            return "{}";
        }
    }

    private static AnswerSheetRepository createNoOpAnswerSheetRepository() {
        return (AnswerSheetRepository) Proxy.newProxyInstance(
                AnswerSheetRepository.class.getClassLoader(),
                new Class<?>[]{AnswerSheetRepository.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "AnswerSheetRepositoryProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ExamRepository createNoOpExamRepository() {
        return (ExamRepository) Proxy.newProxyInstance(
                ExamRepository.class.getClassLoader(),
                new Class<?>[]{ExamRepository.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "ExamRepositoryProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StorageService createNoOpStorageService() {
        return new StorageService(new FileStorageUtil(createNoOpS3Client(), "planbookai-storage", "https://demo.supabase.co"));
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
