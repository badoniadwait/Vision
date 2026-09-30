package com.project.vision.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.project.vision.dto.StudentDto;
import com.project.vision.entity.Student;
import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;

import com.project.vision.service.StudentService;

@Controller 
public class StudentRegistrationController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private AdminRepo adminRepo;

    @GetMapping("/register-student")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "registerStudent";
    }

    @PostMapping("/register-student")
    public String registerStudent(StudentDto student, Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/admin/login";
        }

        String username = authentication.getName();
        Admin admin = adminRepo.findByEmailIgnoreCase(username)
                .or(() -> adminRepo.findByName(username))
                .orElse(null);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsVerified())) {
            return "redirect:/admin/profile?verificationRequired";
        }

        studentService.registerStudent(student, username);
        return "redirect:/";
    }

}
