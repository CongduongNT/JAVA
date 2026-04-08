package com.planbookai.backend.component;

import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.AiPromptTemplate;
import com.planbookai.backend.model.entity.SubscriptionPackage;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.SubscriptionPackageRepository;
import com.planbookai.backend.repository.UserRepository;
import com.planbookai.backend.repository.AiPromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime; 
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SubscriptionPackageRepository packageRepository;
    private final AiPromptTemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
        seedPackages();
        seedPromptTemplates();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            Arrays.stream(Role.RoleName.values()).forEach(roleName -> {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description("Default role for " + roleName)
                        .build());
            });
        }
    }

    private void seedUsers() {
        // Encode "admin" password using the application's PasswordEncoder
        String encodedAdminPass = passwordEncoder.encode("admin");

        ensureUser("admin@planbookai.com", "System Admin", Role.RoleName.ADMIN, encodedAdminPass);
        ensureUser("manager@planbookai.com", "System Manager", Role.RoleName.MANAGER, encodedAdminPass);
        ensureUser("staff@planbookai.com", "Educational Staff", Role.RoleName.STAFF, encodedAdminPass);
        ensureUser("teacher@planbookai.com", "HighSchool Teacher", Role.RoleName.TEACHER, encodedAdminPass);
    }

    private void ensureUser(String email, String fullName, Role.RoleName roleName, String encodedPassword) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            userRepository.save(User.builder()
                    .fullName(fullName).email(email)
                    .passwordHash(encodedPassword).role(role)
                    .isActive(true).emailVerified(true).build());
        } else {
            boolean updated = false;
            // Force role update if mismatch
            if (user.getRole() == null || user.getRole().getName() != roleName) {
                user.setRole(role);
                updated = true;
            }
            // Use passwordEncoder.matches to check if we need to update the hash
            // If the current hash doesn't match "admin", update it to the new hash
            if (!passwordEncoder.matches("admin", user.getPasswordHash())) {
                user.setPasswordHash(encodedPassword);
                updated = true;
            }
            
            if (updated) {
                userRepository.save(user);
            }
        }
    }

    private void seedPackages() {
        User admin = userRepository.findByEmail("admin@planbookai.com").orElse(null);
        if (admin == null) return;

        List<SubscriptionPackage> existingPackages = packageRepository.findAll();

        ensurePackage(existingPackages, "FREE", "Basic plan for new teachers", BigDecimal.ZERO, 30, "{\"lesson_plans\": 5, \"exams\": 2}", admin);
        ensurePackage(existingPackages, "PRO", "Professional plan with AI features", new BigDecimal("199000"), 30, "{\"lesson_plans\": 50, \"exams\": 20, \"ai_generation\": true}", admin);
        ensurePackage(existingPackages, "PREMIUM", "Unlimited enterprise plan", new BigDecimal("499000"), 365, "{\"lesson_plans\": \"unlimited\", \"exams\": \"unlimited\", \"ocr_grading\": true}", admin);
    }

    private void ensurePackage(List<SubscriptionPackage> existingPackages, String name, String description, BigDecimal price, int durationDays, String features, User createdBy) {
        if (existingPackages.stream().noneMatch(p -> p.getName().equals(name))) {
            packageRepository.save(SubscriptionPackage.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .durationDays(durationDays)
                    .features(features)
                    .isActive(true)
                    .createdBy(createdBy)
                    .build());
        }
    }

    private void seedPromptTemplates() {
        User admin = userRepository.findByEmail("admin@planbookai.com").orElse(null);
        if (admin == null) return;

        List<AiPromptTemplate> existing = templateRepository.findAll();

        ensureTemplate(existing, "Tạo câu hỏi trắc nghiệm", "QUESTION_GEN", 
            "Bạn là một chuyên gia giáo dục. Hãy tạo 5 câu hỏi trắc nghiệm về chủ đề {{topic}} với độ khó mức độ {{level}}. Yêu cầu mỗi câu hỏi có 4 phương án lựa chọn và có đáp án giải thích chi tiết.", 
            "topic,level", admin);

        ensureTemplate(existing, "Soạn giáo án mẫu", "LESSON_PLAN_GEN", 
            "Hãy lập kế hoạch bài dạy chi tiết cho bài {{topic}}. Mức độ nhận thức: {{level}}.", 
            "topic,level", admin);
    }

    private void ensureTemplate(List<AiPromptTemplate> existing, String title, String purpose, String text, String vars, User admin) {
        // Kiểm tra xem mẫu đã tồn tại theo tiêu đề chưa để tránh lặp lại
        if (existing.stream().noneMatch(t -> t.getTitle().equals(title))) {
            templateRepository.save(AiPromptTemplate.builder()
                    .title(title)
                    .purpose(purpose)
                    .promptText(text)
                    .variables(vars)
                    .status("APPROVED")
                    .createdBy(admin)
                    .createdAt(LocalDateTime.now())
                    .build());
            System.out.println("✅ DataSeeder: Đã tạo template: " + title + " (Purpose: " + purpose + ", Status: APPROVED)");
        }
    }
}
