package com.project.vision.controller;

import java.util.List;
import java.util.Map;

import com.project.vision.dto.QuizDto;
import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;
import com.project.vision.service.QuizService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import com.project.vision.dto.QuizQuestionDto;
import com.project.vision.dto.QuizResultsDto;

@Controller
public class QuizController {

    private final QuizService quizService;
    private final AdminRepo adminRepo;

    public QuizController(QuizService quizService, AdminRepo adminRepo) {
        this.quizService = quizService;
        this.adminRepo = adminRepo;
    }

    @GetMapping("/upload-quiz")
    public String quizPage(
            @RequestParam(required = false) Integer questions,
            org.springframework.ui.Model model) {

        if (questions != null && (questions < 1 || questions > 20)) {
            return "redirect:/";
        }

        model.addAttribute("numberOfQuestions", questions);

        return "quiz";
    }

    @PostMapping("/admin/create-quiz")
    public String createQuiz(
            @RequestParam int numberOfQuestions,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/admin/login";
        }

        if (numberOfQuestions < 1 || numberOfQuestions > 20) {
            return "redirect:/?error=invalidQuestions";
        }

        String username = authentication.getName();

        Admin admin = adminRepo.findByEmailIgnoreCase(username)
                .or(() -> adminRepo.findByName(username))
                .orElse(null);

        if (admin == null || !Boolean.TRUE.equals(admin.getIsVerified())) {
            return "redirect:/admin/profile?verificationRequired";
        }

        return "redirect:/upload-quiz?questions=" + numberOfQuestions;
    }

    @PostMapping("/upload-quiz")
    public String uploadQuiz(
            @ModelAttribute QuizDto quizDto,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/admin/login";
        }

        Admin admin = adminRepo.findByEmailIgnoreCase(authentication.getName())
                .or(() -> adminRepo.findByName(authentication.getName()))
                .orElse(null);

        if (admin == null || !Boolean.TRUE.equals(admin.getIsVerified())) {
            return "redirect:/admin/profile?verificationRequired";
        }

        quizService.saveQuiz(quizDto);
        return "redirect:/";
    }

    @PostMapping("/api/quiz")
    @ResponseBody
    public QuizDto fetchQuiz(@RequestHeader("id") String id) {

        QuizDto res = quizService.getQuizById(id);

        for (QuizQuestionDto questionDto : res.getQuestions()) {
            questionDto.setAnswer(null);
        }
        return res;
    }

    @PostMapping("/api/quiz/submit")
    @ResponseBody
    public QuizResultsDto submitQuiz(@RequestHeader("id") String id, @RequestBody Map<String, String> answers,
            Authentication authentication) {

        // upload quiz to database
        System.out.println("Fetching quiz with ID: " + id);

        return quizService.validateAnswers(id, authentication.getName(), answers);
    }

    @PostMapping("/api/quiz-results")
    @ResponseBody
    public List<QuizResultsDto> fetchStudentQuizzesResults(Authentication authentication) {

        return quizService.getQuizResultsForStudent(authentication.getName());
    }

    @PostMapping("/api/quiz-result")
    @ResponseBody
    public QuizResultsDto fetchStudentQuizResult(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String quizId = request.get("quizId");

        if (quizId == null || quizId.isBlank()) {
            throw new IllegalArgumentException("Quiz ID is required.");
        }

        return quizService.getQuizResultForStudent(
                authentication.getName(),
                quizId
        );
    }

}
