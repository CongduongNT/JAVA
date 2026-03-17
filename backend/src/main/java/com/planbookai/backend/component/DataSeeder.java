package com.planbookai.backend.component;

import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.SubscriptionPackage;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.SubscriptionPackageRepository;
import com.planbookai.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SubscriptionPackageRepository packageRepository;
    private final PasswordEncoder passwordEncoder;

    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
        seedPackages();
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
        if (packageRepository.count() == 0) {
            User admin = userRepository.findByEmail("admin@planbookai.com")
                    .orElse(null);

            packageRepository.saveAll(Arrays.asList(
                SubscriptionPackage.builder()
                        .name("FREE")
                        .description("Basic plan for new teachers")
                        .price(BigDecimal.ZERO)
                        .durationDays(30)
                        .features("{\"lesson_plans\": 5, \"exams\": 2}")
                        .isActive(true)
                        .createdBy(admin)
                        .build(),
                SubscriptionPackage.builder()
                        .name("PRO")
                        .description("Professional plan with AI features")
                        .price(new BigDecimal("199000"))
                        .durationDays(30)
                        .features("{\"lesson_plans\": 50, \"exams\": 20, \"ai_generation\": true}")
                        .isActive(true)
                        .createdBy(admin)
                        .build(),
                SubscriptionPackage.builder()
                        .name("PREMIUM")
                        .description("Unlimited enterprise plan")
                        .price(new BigDecimal("499000"))
                        .durationDays(365)
                        .features("{\"lesson_plans\": \"unlimited\", \"exams\": \"unlimited\", \"ocr_grading\": true}")
                        .isActive(true)
                        .createdBy(admin)
                        .build()
            ));
        }
    }
}
