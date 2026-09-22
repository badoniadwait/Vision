package com.project.vision.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;

@Controller
public class AdminController {

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "adminLogin";
    }

    @GetMapping("/admin/signup")
    public String adminSignup() {
        return "adminSignup";
    }

    @PostMapping("/admin/signup")
    public String adminSignup(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String name,
                              @RequestParam String email,
                              @RequestParam String subject,
                              @RequestParam String password) {
        Admin admin = new Admin();
        admin.setId(UUID.randomUUID().toString());

        String resolvedName = (name != null && !name.isBlank()) ? name : username;
        if (resolvedName == null || resolvedName.isBlank()) {
            resolvedName = email;
        }
        admin.setName(resolvedName);
        admin.setSubject(subject);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepo.save(admin);
        return "redirect:/admin/login";
    }
}
