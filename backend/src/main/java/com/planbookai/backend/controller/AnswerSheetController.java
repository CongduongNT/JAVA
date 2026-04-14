package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.UploadAnswerSheetRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AnswerSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/answer-sheets")
@Tag(name = "Answer Sheets", description = "Teacher answer sheet upload management")
public class AnswerSheetController {

    private final AnswerSheetService answerSheetService;

    public AnswerSheetController(AnswerSheetService answerSheetService) {
        this.answerSheetService = answerSheetService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Upload scanned answer sheets",
            description = """
                    Uploads one or more scanned answer sheet files for an exam owned by the current teacher.

                    Supported multipart fields:
                    - `exam_id`: preferred exam identifier from SA contract
                    - `examId`: compatibility alias
                    - `files`: repeated multipart files
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Answer sheets uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid multipart payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only owner teacher can upload answer sheets", content = @Content),
            @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content),
    })
    public ResponseEntity<List<AnswerSheetDTO>> uploadAnswerSheets(
            @Parameter(description = "Exam identifier in snake_case", example = "12")
            @RequestParam(value = "exam_id", required = false) Long snakeCaseExamId,
            @Parameter(description = "Exam identifier in camelCase compatibility mode", example = "12")
            @RequestParam(value = "examId", required = false) Long camelCaseExamId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User user) {

        UploadAnswerSheetRequest request = new UploadAnswerSheetRequest();
        request.setExamId(resolveExamId(snakeCaseExamId, camelCaseExamId));
        request.setFiles(files);

        return ResponseEntity.status(201).body(answerSheetService.uploadAnswerSheets(request, user));
    }

    private Long resolveExamId(Long snakeCaseExamId, Long camelCaseExamId) {
        if (snakeCaseExamId != null && camelCaseExamId != null && !snakeCaseExamId.equals(camelCaseExamId)) {
            throw new IllegalArgumentException("Provide either exam_id or examId with the same value");
        }
        return snakeCaseExamId != null ? snakeCaseExamId : camelCaseExamId;
    }
}
