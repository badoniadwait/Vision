package com.project.vision.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.vision.dto.QuizQuestionDto;
import com.project.vision.entity.Admin;
import com.project.vision.entity.Question;
import com.project.vision.entity.Quiz;
import com.project.vision.repo.AdminRepo;
import com.project.vision.repo.QuizRepository;

@Controller
public class MobileAppController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AdminRepo adminRepo;

    @PostMapping("/api/fetch-quizzes")
    @ResponseBody
    public List<QuizQuestionDto> fetchQuizzes(Authentication authentication) {

        // Check whether an admin is logged in
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || authentication.getName().equals("anonymousUser")) {
            throw new RuntimeException("admin Unauthorized");
        }

        // Find the currently logged-in admin
        String principal = authentication.getName();

        Admin admin = adminRepo.findByEmail(principal)
                .or(() -> adminRepo.findByName(principal))
                .orElse(null);

        if (admin == null) {
            throw new RuntimeException("admin Unauthorized");
        }

        // Fetch quizzes created by this admin
        List<Quiz> quizzes = quizRepository
                .findByAdmin_IdOrderByCreatedAtDesc(admin.getId());

        List<QuizQuestionDto> res = new ArrayList<>();

        for (Quiz quiz : quizzes) {

            for (Question question : quiz.getQuestions()) {

                QuizQuestionDto dto = new QuizQuestionDto();

                dto.setQuestion(question.getQuestion());

                dto.setOptions(List.of(
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD()
                ));

                dto.setAnswer(question.getAnswer().toString());

                res.add(dto);
            }
        }

        return res;
    }
}