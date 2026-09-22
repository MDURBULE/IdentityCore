package com.IdentityCore.model.entity;


import java.time.Instant;

import com.IdentityCore.model.constant.CredentialType;

public class Credential {
    private Long id;
    private Long userId;
    private CredentialType credentialType;
    private String secretHash;
    private String providerReference;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public Credential() {}

    public Credential(Long id, Long userId, CredentialType credentialType, String secretHash, String providerReference, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.credentialType = credentialType;
        this.secretHash = secretHash;
        this.providerReference = providerReference;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CredentialType getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(CredentialType credentialType) {
        this.credentialType = credentialType;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
