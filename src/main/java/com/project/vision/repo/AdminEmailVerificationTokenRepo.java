package com.project.vision.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.vision.entity.AdminEmailVerificationToken;

public interface AdminEmailVerificationTokenRepo extends JpaRepository<AdminEmailVerificationToken, String> {
    Optional<AdminEmailVerificationToken> findByAdmin_Id(String adminId);
    Optional<AdminEmailVerificationToken> findByTokenHash(String tokenHash);
}
