package com.project.vision.controller;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;

@Controller
public class AdminController {

    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepo adminRepo, PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/login")
    public String adminLogin(Authentication authentication) {
        return isAuthenticated(authentication) ? "redirect:/" : "adminLogin";
    }

    @GetMapping("/admin/signup")
    public String adminSignup(Authentication authentication) {
        return isAuthenticated(authentication) ? "redirect:/" : "adminSignup";
    }

    @PostMapping("/admin/register")
    public String adminRegister(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String password,
            Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/";
        }

        String normalizedName = name.trim();
        String normalizedEmail = email.trim();
        String normalizedSubject = subject.trim();
        if (!StringUtils.hasText(normalizedName)
                || !StringUtils.hasText(normalizedEmail)
                || !StringUtils.hasText(normalizedSubject)
                || !StringUtils.hasText(password)
                || adminRepo.findByEmail(normalizedEmail).isPresent()
                || adminRepo.findByName(normalizedName).isPresent()) {
            return "redirect:/admin/signup?error";
        }

        Admin admin = new Admin();
        admin.setId(UUID.randomUUID().toString());
        admin.setName(normalizedName);
        admin.setEmail(normalizedEmail);
        admin.setSubject(normalizedSubject);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepo.save(admin);

        return "redirect:/admin/login?registered";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
