package com.IdentityCore.model.entity;

import java.time.Instant;
import java.util.UUID;

import com.IdentityCore.model.constant.UserStatus;

public class User {
    private Long id;
    private UUID publicId;
    private String email;
    private String normalizedEmail;
    private UserStatus status;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    public User() {}

    public User(Long id, UUID publicId, String email, String normalizedEmail, UserStatus status, boolean emailVerified, Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.publicId = publicId;
        this.email = email;
        this.normalizedEmail = normalizedEmail;
        this.status = status;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNormalizedEmail() {
        return normalizedEmail;
    }

    public void setNormalizedEmail(String normalizedEmail) {
        this.normalizedEmail = normalizedEmail;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}

