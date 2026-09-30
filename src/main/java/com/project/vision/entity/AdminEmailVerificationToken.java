package com.project.vision.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_email_verification_tokens")
public class AdminEmailVerificationToken {
    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "admin_id", nullable = false, unique = true)
    private Admin admin;

    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant consumedAt;

    public AdminEmailVerificationToken() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
}
