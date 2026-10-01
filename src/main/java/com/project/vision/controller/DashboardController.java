package com.project.vision.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.project.vision.entity.Admin;
import com.project.vision.entity.Quiz;
import com.project.vision.repo.AdminRepo;
import com.project.vision.repo.QuizRepository;

@Controller
public class DashboardController {

    private final AdminRepo adminRepo;
    private final QuizRepository quizRepository;

    public DashboardController(AdminRepo adminRepo, QuizRepository quizRepository) {
        this.adminRepo = adminRepo;
        this.quizRepository = quizRepository;
    }

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String dashboard(Authentication authentication, Model model) {

        // Check whether an admin is logged in
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || authentication.getName().equals("anonymousUser")) {
            return "redirect:/admin/login";
        }

        // Find the currently logged-in admin
        String principal = authentication.getName();

        Admin admin = adminRepo.findByEmail(principal)
                .or(() -> adminRepo.findByName(principal))
                .orElse(null);

        if (admin == null) {
            return "redirect:/admin/login";
        }

        // Fetch quizzes belonging to this admin
        List<Quiz> quizzes = quizRepository
                .findByAdmin_IdOrderByCreatedAtDesc(admin.getId());

        // Send data to dashboard.html
        model.addAttribute("adminName", admin.getName());
        model.addAttribute("quizzes", quizzes);

        return "dashboard";
    }

    @PostMapping("/admin/quizzes/{id}/delete")
    @Transactional
    public String deleteQuiz(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            return "redirect:/admin/login";
        }

        // Find the logged-in admin
        String principal = authentication.getName();

        Optional<Admin> adminOptional = adminRepo.findByEmail(principal)
                .or(() -> adminRepo.findByName(principal));

        if (adminOptional.isEmpty()) {
            return "redirect:/admin/login";
        }

        Admin admin = adminOptional.get();

        // Only allow deletion of quizzes owned by this admin
        Optional<Quiz> quizOptional = quizRepository
                .findByIdAndAdmin_Id(id, admin.getId());

        if (quizOptional.isPresent()) {
            quizRepository.delete(quizOptional.get());
        }

        return "redirect:/";
    }
}
