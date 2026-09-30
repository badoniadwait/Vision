package com.project.vision.service;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.vision.entity.Admin;
import com.project.vision.entity.AdminOAuthAccount;
import com.project.vision.repo.AdminOAuthAccountRepo;
import com.project.vision.repo.AdminRepo;

@Service
public class GitHubRegistrationService {
    private final AdminRepo adminRepo;
    private final AdminOAuthAccountRepo oauthRepo;
    private final PasswordEncoder passwordEncoder;

    public GitHubRegistrationService(AdminRepo adminRepo, AdminOAuthAccountRepo oauthRepo, PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.oauthRepo = oauthRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Admin complete(String name, String subject, String email, String githubId) {
        if (oauthRepo.findByProviderAndProviderUserId("GITHUB", githubId).isPresent())
            throw new IllegalStateException("This GitHub account has already been registered.");
        if (adminRepo.findByEmailIgnoreCase(email).isPresent())
            throw new IllegalStateException("An admin already uses this email. Sign in to that account to link identities.");
        Admin admin = new Admin();
        admin.setId(UUID.randomUUID().toString());
        admin.setName(name.trim());
        admin.setEmail(email);
        admin.setSubject(subject.trim());
        admin.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setIsVerified(false);
        adminRepo.save(admin);
        oauthRepo.save(new AdminOAuthAccount(UUID.randomUUID().toString(), "GITHUB", githubId, admin));
        return admin;
    }
}
