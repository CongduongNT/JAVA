package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.List;
import java.util.Collections;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.time.LocalDateTime;

/**
 * User Entity
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails { // Implement UserDetails interface

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "full_name")
    @NotNull(message = "Full name cannot be null")
    private String fullName;

    @Column(unique = true, nullable = false)
    @NotNull(message = "Email cannot be null")
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotNull(message = "Password hash cannot be null")
    private String passwordHash;

    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == null || this.role.getName() == null) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.getName().name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive != null && this.isActive;
    }

    // Manual getters to bypass Lombok compilation bugs
    public String getEmail() {
        return this.email;
    }

    public Role getRole() {
        return this.role;
    }

    
}
