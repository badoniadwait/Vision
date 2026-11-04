package com.project.vision.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;
import com.project.vision.security.StudentJwtService;

@RestController
@RequestMapping("/api/auth")
public class MobileAuthController {

    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final StudentJwtService jwtService;

    public MobileAuthController(
            AdminRepo adminRepo,
            PasswordEncoder passwordEncoder,
            StudentJwtService jwtService) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            return error(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        Admin admin = adminRepo.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (admin == null || !passwordEncoder.matches(request.password(), admin.getPassword())) {
            return error(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return ResponseEntity.ok(authResponse(admin));
    }

    @PostMapping("/admin/register")
    public ResponseEntity<?> register(@RequestBody AdminRegisterRequest request) {
        if (request == null
                || !StringUtils.hasText(request.name())
                || !StringUtils.hasText(request.email())
                || !StringUtils.hasText(request.subject())
                || !StringUtils.hasText(request.password())) {
            return error(HttpStatus.BAD_REQUEST, "Name, email, subject, and password are required.");
        }

        String email = request.email().trim();
        if (adminRepo.findByEmailIgnoreCase(email).isPresent()
                || adminRepo.findByName(request.name().trim()).isPresent()) {
            return error(HttpStatus.CONFLICT, "An admin with this email or name already exists.");
        }

        Admin admin = new Admin();
        admin.setId(UUID.randomUUID().toString());
        admin.setName(request.name().trim());
        admin.setEmail(email);
        admin.setSubject(request.subject().trim());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setIsVerified(false);
        adminRepo.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse(admin));
    }

    private Map<String, Object> authResponse(Admin admin) {
        return Map.of(
                "success", true,
                "token", jwtService.generateToken(admin.getEmail(), "ADMIN"),
                "admin", Map.of(
                        "id", admin.getId(),
                        "name", admin.getName(),
                        "email", admin.getEmail(),
                        "subject", admin.getSubject() == null ? "" : admin.getSubject(),
                        "verified", Boolean.TRUE.equals(admin.getIsVerified())));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("success", "false", "message", message));
    }

    public record AdminLoginRequest(String email, String password) {}

    public record AdminRegisterRequest(String name, String email, String subject, String password) {}
}
