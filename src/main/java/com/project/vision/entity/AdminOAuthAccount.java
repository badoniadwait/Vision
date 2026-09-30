package com.project.vision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "admin_oauth_accounts", uniqueConstraints = @UniqueConstraint(
        name = "uk_admin_oauth_provider_user", columnNames = {"provider", "provider_user_id"}))
public class AdminOAuthAccount {
    @Id
    private String id;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "provider_user_id", nullable = false, length = 128)
    private String providerUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    protected AdminOAuthAccount() {}

    public AdminOAuthAccount(String id, String provider, String providerUserId, Admin admin) {
        this.id = id;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.admin = admin;
    }

    public String getId() { return id; }
    public String getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }
    public Admin getAdmin() { return admin; }
}
