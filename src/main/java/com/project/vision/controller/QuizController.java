package com.project.vision.controller;

import com.project.vision.dto.QuizDto;
import com.project.vision.service.QuizService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/upload-quiz")
    public String quizPage() {
        return "quiz";
    }

    @PostMapping("/upload-quiz")
    public String uploadQuiz(@ModelAttribute QuizDto quizDto) {
        try {
            quizService.saveQuiz(quizDto);
            return "redirect:/";
        } catch (RuntimeException exception) {
            return "redirect:/upload-quiz?error";
        }
    }
}
