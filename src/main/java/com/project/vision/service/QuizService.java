package com.project.vision.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.vision.dto.QuizDto;
import com.project.vision.dto.QuizQuestionDto;
import com.project.vision.entity.Admin;
import com.project.vision.entity.Question;
import com.project.vision.entity.Quiz;
import com.project.vision.repo.AdminRepo;
import com.project.vision.repo.QuizRepository;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final AdminRepo adminRepo;
    int size = 2;

    public QuizService(QuizRepository quizRepository, AdminRepo adminRepo) {
        this.quizRepository = quizRepository;
        this.adminRepo = adminRepo;
    }

    @Transactional
    public Quiz saveQuiz(QuizDto quizDto) {
        validate(quizDto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("An authenticated admin is required.");
        }

        String principal = authentication.getName();
        Admin admin = adminRepo.findByEmail(principal)
                .or(() -> adminRepo.findByName(principal))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated admin was not found."));

        Instant now = Instant.now();
        Quiz quiz = new Quiz();
        quiz.setAdmin(admin);
        quiz.setDurationMinutes(quizDto.getDurationMinutes());
        quiz.setExpiresAt(now.plusSeconds(quizDto.getDurationMinutes() * 60L));

        for (QuizQuestionDto questionDto : quizDto.getQuestions()) {
            Question question = new Question();
            question.setQuestion(questionDto.getQuestion().trim());
            question.setOptionA(questionDto.getOptions().get(0).trim());
            question.setOptionB(questionDto.getOptions().get(1).trim());
            question.setOptionC(questionDto.getOptions().get(2).trim());
            question.setOptionD(questionDto.getOptions().get(3).trim());
            question.setAnswer(Integer.valueOf(questionDto.getAnswer()));
            quiz.addQuestion(question);
        }

        return quizRepository.save(quiz);
    }

    private void validate(QuizDto quizDto) {
        if (quizDto == null || quizDto.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("A valid duration is required.");
        }
        List<QuizQuestionDto> questions = quizDto.getQuestions();
        if (questions == null || questions.size() != size) {
            throw new IllegalArgumentException("Exactly " + size + " questions are required.");
        }

        for (QuizQuestionDto question : questions) {
            if (question == null || isBlank(question.getQuestion())) {
                throw new IllegalArgumentException("Every question must have question text.");
            }
            List<String> options = question.getOptions();
            if (options == null || options.size() != 4 || options.stream().anyMatch(this::isBlank)) {
                throw new IllegalArgumentException("Every question must have four non-empty options.");
            }
            if (isBlank(question.getAnswer())) {
                throw new IllegalArgumentException("Every question must have a correct answer.");
            }
            try {
                int answer = Integer.parseInt(question.getAnswer());
                if (answer < 0 || answer > 3) {
                    throw new IllegalArgumentException("Correct answer must be between 0 and 3.");
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Correct answer must be between 0 and 3.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
