package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.UploadAnswerSheetRequest;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import com.planbookai.backend.repository.ExamRepository;
import com.planbookai.backend.service.AnswerSheetService;
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
        AnswerSheetDTO dto = new AnswerSheetDTO();
        dto.setId(1L);
        dto.setExamId(12L);
        dto.setTeacherId(teacher.getId());
        dto.setFileUrl("https://demo.supabase.co/storage/sheet-01.png");
        dto.setOcrStatus(AnswerSheet.OcrStatus.PENDING);
        dto.setUploadedAt(LocalDateTime.now());
        List<AnswerSheetDTO> expected = List.of(dto);
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.response = expected;
        AnswerSheetController controller = new AnswerSheetController(service);

        ResponseEntity<List<AnswerSheetDTO>> response = controller.uploadAnswerSheets(
                12L,
                null,
                List.of(file),
                teacher);

        assertEquals(201, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertSame(teacher, service.user);
        assertEquals(12L, service.request.getExamId());
        assertSame(file, service.request.getFiles().get(0));
    }

    @Test
    void uploadAnswerSheetsDelegatesToServiceWithCamelCaseExamId() {
        User teacher = buildTeacher(5L);
        MockMultipartFile file = buildFile("sheet-01.png");
        RecordingAnswerSheetService service = new RecordingAnswerSheetService();
        service.response = List.of();
        AnswerSheetController controller = new AnswerSheetController(service);

        ResponseEntity<List<AnswerSheetDTO>> response = controller.uploadAnswerSheets(
                null,
                15L,
                List.of(file),
                teacher);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(15L, service.request.getExamId());
        assertSame(file, service.request.getFiles().get(0));
    }

    @Test
    void uploadAnswerSheetsRejectsAmbiguousExamId() {
        AnswerSheetController controller = new AnswerSheetController(new RecordingAnswerSheetService());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadAnswerSheets(10L, 11L, List.of(buildFile("sheet-01.png")), buildTeacher(5L)));

        assertEquals("Provide either exam_id or examId with the same value", exception.getMessage());
    }

    @Test
    void uploadEndpointRequiresTeacherRole() throws NoSuchMethodException {
        Method method = AnswerSheetController.class.getMethod(
                "uploadAnswerSheets",
                Long.class,
                Long.class,
                List.class,
                User.class);

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

    private MockMultipartFile buildFile(String filename) {
        return new MockMultipartFile("files", filename, "image/png", "content".getBytes());
    }

    private static final class RecordingAnswerSheetService extends AnswerSheetService {
        private UploadAnswerSheetRequest request;
        private User user;
        private List<AnswerSheetDTO> response;

        private RecordingAnswerSheetService() {
            super(createNoOpAnswerSheetRepository(), createNoOpExamRepository(), createNoOpStorageService());
        }

        @Override
        public List<AnswerSheetDTO> uploadAnswerSheets(UploadAnswerSheetRequest request, User user) {
            this.request = request;
            this.user = user;
            return response;
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
