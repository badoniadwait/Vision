package com.project.vision.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.vision.dto.QuizDto;
import com.project.vision.dto.QuizQuestionDto;
import com.project.vision.dto.QuizResultsDto;
import com.project.vision.entity.Admin;
import com.project.vision.entity.Question;
import com.project.vision.entity.Quiz;
import com.project.vision.entity.QuizResults;
import com.project.vision.repo.AdminRepo;
import com.project.vision.repo.QuizRepository;
import com.project.vision.repo.QuizResultsRepository;
import com.project.vision.repo.StudentRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final AdminRepo adminRepo;
    private final StudentRepo studentRepo;
    private final QuizResultsRepository quizResultsRepository;
    int size = 2;

    public QuizService(QuizRepository quizRepository, AdminRepo adminRepo, StudentRepo studentRepo,
            QuizResultsRepository quizResultsRepository) {
        this.quizRepository = quizRepository;
        this.adminRepo = adminRepo;
        this.studentRepo = studentRepo;
        this.quizResultsRepository = quizResultsRepository;
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

    @Transactional(readOnly = true)
    public QuizDto getQuizById(String id) {

        Long quizId;

        try {
            quizId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quiz ID.");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found."));

        QuizDto quizDto = new QuizDto();
        quizDto.setDurationMinutes(quiz.getDurationMinutes());

        List<QuizQuestionDto> questions = new ArrayList<>();

        for (Question question : quiz.getQuestions()) {

            QuizQuestionDto questionDto = new QuizQuestionDto();

            questionDto.setQuestion(question.getQuestion());

            questionDto.setOptions(Arrays.asList(
                    question.getOptionA(),
                    question.getOptionB(),
                    question.getOptionC(),
                    question.getOptionD()
            ));

            questionDto.setAnswer(question.getAnswer().toString());

            questions.add(questionDto);
        }

        quizDto.setQuestions(questions);

        return quizDto;
    }

    @Transactional
    public QuizResultsDto validateAnswers(String id, String studentSapId, Map<String, String> answers) {
        QuizDto quiz = getQuizById(id);
        List<QuizQuestionDto> questions = quiz.getQuestions();
        studentRepo.findById(studentSapId)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated student was not found."));
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestionDto question = questions.get(i);
            String submittedAnswer = answers == null ? null : answers.get(String.valueOf(i + 1));
            int correctAnswer;
            try {
                correctAnswer = Integer.parseInt(question.getAnswer());
            } catch (NumberFormatException exception) {
                continue;
            }
            List<String> options = question.getOptions();
            if (submittedAnswer != null && options != null
                    && correctAnswer >= 0 && correctAnswer < options.size()
                    && correctAnswer < 4 && submittedAnswer.equals(options.get(correctAnswer))) {
                score++;
            }
        }

        QuizResults result = new QuizResults();
        result.setQuizId(id);
        result.setStudentId(studentSapId);
        result.setMarksObtained(score);
        result.setTotalMarks(questions.size());
        quizResultsRepository.save(result);
        return new QuizResultsDto(id, score, questions.size());
    }

    public List<QuizResultsDto> getQuizResultsForStudent(String studentId) {
        return quizResultsRepository.findByStudentId(studentId)
                .stream()
                .map(result -> new QuizResultsDto(
                        result.getQuizId(),
                        result.getMarksObtained(),
                        result.getTotalMarks()
                ))
                .toList();
    }
}
