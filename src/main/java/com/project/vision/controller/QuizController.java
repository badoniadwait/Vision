package com.project.vision.controller;

import com.project.vision.dto.QuizDto;
import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;
import com.project.vision.service.QuizService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;

@Controller
public class QuizController {

    private final QuizService quizService;
    private final AdminRepo adminRepo;

    public QuizController(QuizService quizService, AdminRepo adminRepo) {
        this.quizService = quizService;
        this.adminRepo = adminRepo;
    }

    @GetMapping("/upload-quiz")
    public String quizPage() {
        return "quiz";
    }

    @PostMapping("/upload-quiz")
    public String uploadQuiz(
            @ModelAttribute QuizDto quizDto,
            Authentication authentication) {

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

        try {
            quizService.saveQuiz(quizDto);
            return "redirect:/";
        } catch (RuntimeException exception) {
            return "redirect:/upload-quiz?error";
        }
    }
}
