package com.project.vision.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.vision.entity.Admin;
import com.project.vision.entity.AdminEmailVerificationToken;
import com.project.vision.repo.AdminEmailVerificationTokenRepo;
import com.project.vision.repo.AdminRepo;

@Service
public class AdminEmailVerificationService {
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(20);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AdminRepo adminRepo;
    private final AdminEmailVerificationTokenRepo tokenRepo;
    private final JavaMailSender mailSender;

    @Value("${app.email-verification.base-url:http://localhost:8080}")
    private String baseUrl;
    @Value("${spring.mail.username:}")
    private String from;

    public AdminEmailVerificationService(AdminRepo adminRepo, AdminEmailVerificationTokenRepo tokenRepo,
            JavaMailSender mailSender) {
        this.adminRepo = adminRepo;
        this.tokenRepo = tokenRepo;
        this.mailSender = mailSender;
    }

    @Transactional
    public void requestVerification(String email) {
        Admin admin = adminRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found."));
        if (Boolean.TRUE.equals(admin.getIsVerified())) {
            throw new IllegalStateException("This email is already verified.");
        }
        Instant now = Instant.now();
        AdminEmailVerificationToken record = tokenRepo.findByAdmin_Id(admin.getId())
                .orElseGet(AdminEmailVerificationToken::new);
        if (record.getCreatedAt() != null && record.getCreatedAt().plus(RESEND_COOLDOWN).isAfter(now)) {
            throw new IllegalStateException("Please wait before requesting another verification email.");
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        record.setId(record.getId() == null ? UUID.randomUUID().toString() : record.getId());
        record.setAdmin(admin);
        record.setTokenHash(hash(rawToken));
        record.setCreatedAt(now);
        record.setExpiresAt(now.plus(TOKEN_LIFETIME));
        record.setConsumedAt(null);
        tokenRepo.save(record);

        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) message.setFrom(from);
        message.setTo(admin.getEmail());
        message.setSubject("Verify Your Vision Account Email");
        message.setText("You requested email verification for your Vision admin account.\n\n"
                + "Verify your email using this link:\n" + baseUrl.replaceAll("/+$", "")
                + "/admin/verify-email/confirm?token=" + rawToken
                + "\n\nThis link expires in 20 minutes. If you did not request this, you can ignore this email.");
        mailSender.send(message);
    }

    @Transactional
    public boolean confirm(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        var maybeToken = tokenRepo.findByTokenHash(hash(rawToken));
        if (maybeToken.isEmpty()) return false;
        AdminEmailVerificationToken token = maybeToken.get();
        Instant now = Instant.now();
        if (token.getConsumedAt() != null || token.getExpiresAt() == null || !token.getExpiresAt().isAfter(now)) return false;
        Admin admin = token.getAdmin();
        if (admin == null) return false;
        admin.setIsVerified(true);
        token.setConsumedAt(now);
        token.setTokenHash(null);
        adminRepo.save(admin);
        tokenRepo.save(token);
        return true;
    }

    private String hash(String value) {
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to process verification token.", exception);
        }
    }
}
