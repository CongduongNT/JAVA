package com.planbookai.backend.service;

import com.planbookai.backend.dto.CurriculumFrameworkDTO;
import com.planbookai.backend.dto.CurriculumFrameworkRequest;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.CurriculumFramework;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.CurriculumFrameworkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CurriculumFrameworkService {

    private final CurriculumFrameworkRepository frameworkRepository;

    public CurriculumFrameworkService(CurriculumFrameworkRepository frameworkRepository) {
        this.frameworkRepository = frameworkRepository;
    }

    /**
     * Lấy danh sách frameworks với phân trang (cho Admin - tất cả)
     */
    public PageResponse<CurriculumFrameworkDTO> getAllFrameworks(Integer page, Integer size, String subject, String gradeLevel) {
        validatePageRequest(page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CurriculumFramework> frameworks;
        
        if (subject != null && gradeLevel != null) {
            frameworks = frameworkRepository.findBySubjectAndGradeLevel(subject, gradeLevel, pageable);
        } else if (subject != null) {
            frameworks = frameworkRepository.findBySubject(subject, pageable);
        } else if (gradeLevel != null) {
            frameworks = frameworkRepository.findByGradeLevel(gradeLevel, pageable);
        } else {
            frameworks = frameworkRepository.findAll(pageable);
        }
        
        return PageResponse.from(frameworks.map(CurriculumFrameworkDTO::fromEntity));
    }

    /**
     * Lấy danh sách frameworks đã publish (cho Teacher/Staff - chỉ published)
     */
    public PageResponse<CurriculumFrameworkDTO> getPublishedFrameworks(Integer page, Integer size, String subject, String gradeLevel) {
        validatePageRequest(page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CurriculumFramework> frameworks;
        
        if (subject != null && gradeLevel != null) {
            frameworks = frameworkRepository.findBySubjectAndGradeLevelAndIsPublishedTrue(subject, gradeLevel, pageable);
        } else if (subject != null) {
            frameworks = frameworkRepository.findBySubjectAndIsPublishedTrue(subject, pageable);
        } else if (gradeLevel != null) {
            frameworks = frameworkRepository.findByGradeLevelAndIsPublishedTrue(gradeLevel, pageable);
        } else {
            frameworks = frameworkRepository.findByIsPublishedTrue(pageable);
        }
        
        return PageResponse.from(frameworks.map(CurriculumFrameworkDTO::fromEntity));
    }

    /**
     * Lấy danh sách tất cả frameworks đã publish (không phân trang)
     */
    public List<CurriculumFrameworkDTO> getAllPublishedFrameworks() {
        return frameworkRepository.findByIsPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(CurriculumFrameworkDTO::fromEntity)
                .toList();
    }

    /**
     * Lấy chi tiết framework theo ID
     */
    public CurriculumFrameworkDTO getFrameworkById(Integer id) {
        CurriculumFramework framework = findFrameworkOrThrow(id);
        return CurriculumFrameworkDTO.fromEntity(framework);
    }

    /**
     * Tạo mới framework (Admin only)
     */
    @Transactional
    public CurriculumFrameworkDTO createFramework(CurriculumFrameworkRequest request, User admin) {
        CurriculumFramework framework = new CurriculumFramework();
        framework.setTitle(request.getTitle());
        framework.setSubject(request.getSubject());
        framework.setGradeLevel(request.getGradeLevel());
        framework.setDescription(request.getDescription());
        framework.setStructure(request.getStructure());
        framework.setCreatedBy(admin);
        framework.setIsPublished(Boolean.TRUE.equals(request.getIsPublished()));
        framework.setCreatedAt(LocalDateTime.now());
        framework.setUpdatedAt(LocalDateTime.now());
        
        CurriculumFramework saved = frameworkRepository.save(framework);
        return CurriculumFrameworkDTO.fromEntity(saved);
    }

    /**
     * Cập nhật framework (Admin only)
     */
    @Transactional
    public CurriculumFrameworkDTO updateFramework(Integer id, CurriculumFrameworkRequest request) {
        CurriculumFramework framework = findFrameworkOrThrow(id);
        
        framework.setTitle(request.getTitle());
        framework.setSubject(request.getSubject());
        framework.setGradeLevel(request.getGradeLevel());
        framework.setDescription(request.getDescription());
        framework.setStructure(request.getStructure());
        if (request.getIsPublished() != null) {
            framework.setIsPublished(request.getIsPublished());
        }
        framework.setUpdatedAt(LocalDateTime.now());
        
        CurriculumFramework saved = frameworkRepository.save(framework);
        return CurriculumFrameworkDTO.fromEntity(saved);
    }

    /**
     * Xóa framework (Admin only)
     */
    @Transactional
    public void deleteFramework(Integer id) {
        CurriculumFramework framework = findFrameworkOrThrow(id);
        frameworkRepository.delete(framework);
    }

    /**
     * Publish/Unpublish framework (Admin only)
     */
    @Transactional
    public CurriculumFrameworkDTO publishFramework(Integer id, boolean publish) {
        CurriculumFramework framework = findFrameworkOrThrow(id);
        framework.setIsPublished(publish);
        framework.setUpdatedAt(LocalDateTime.now());
        
        CurriculumFramework saved = frameworkRepository.save(framework);
        return CurriculumFrameworkDTO.fromEntity(saved);
    }

    private CurriculumFramework findFrameworkOrThrow(Integer id) {
        return frameworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum framework not found: " + id));
    }

    private void validatePageRequest(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size == null || size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }
}
