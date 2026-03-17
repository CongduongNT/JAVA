package com.planbookai.backend.component;

import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.SubscriptionPackage;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.SubscriptionPackageRepository;
import com.planbookai.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SubscriptionPackageRepository packageRepository;

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
        String defaultPassword = "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GdRphumyjmsu"; // 'admin'

        if (userRepository.findByEmail("admin@planbookai.com").isEmpty()) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN).orElseThrow();
            userRepository.save(User.builder()
                    .fullName("System Admin").email("admin@planbookai.com")
                    .passwordHash(defaultPassword).role(adminRole).isActive(true).emailVerified(true).build());
        }

        if (userRepository.findByEmail("manager@planbookai.com").isEmpty()) {
            Role managerRole = roleRepository.findByName(Role.RoleName.MANAGER).orElseThrow();
            userRepository.save(User.builder()
                    .fullName("System Manager").email("manager@planbookai.com")
                    .passwordHash(defaultPassword).role(managerRole).isActive(true).emailVerified(true).build());
        }

        if (userRepository.findByEmail("staff@planbookai.com").isEmpty()) {
            Role staffRole = roleRepository.findByName(Role.RoleName.STAFF).orElseThrow();
            userRepository.save(User.builder()
                    .fullName("Educational Staff").email("staff@planbookai.com")
                    .passwordHash(defaultPassword).role(staffRole).isActive(true).emailVerified(true).build());
        }

        if (userRepository.findByEmail("teacher@planbookai.com").isEmpty()) {
            Role teacherRole = roleRepository.findByName(Role.RoleName.TEACHER).orElseThrow();
            userRepository.save(User.builder()
                    .fullName("HighSchool Teacher").email("teacher@planbookai.com")
                    .passwordHash(defaultPassword).role(teacherRole).isActive(true).emailVerified(true).build());
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
