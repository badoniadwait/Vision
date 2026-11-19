package com.project.vision.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.vision.entity.Quiz;
import com.project.vision.entity.Student;
import com.project.vision.repo.QuizRepository;
import com.project.vision.repo.StudentRepo;

@RestController
@RequestMapping("/api/student")
public class StudentMobileController {

    private final StudentRepo studentRepo;
    private final QuizRepository quizRepository;

    public StudentMobileController(StudentRepo studentRepo, QuizRepository quizRepository) {
        this.studentRepo = studentRepo;
        this.quizRepository = quizRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        if (!isStudent(authentication)) return unauthorized();

        Student student = studentRepo.findWithAdminBySapId(authentication.getName()).orElse(null);
        if (student == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new StudentProfile(
                student.getSapId(),
                student.getName(),
                student.getAdminId(),
                student.getReferencePhotoPath()));
    }

    @GetMapping("/quizzes")
    public ResponseEntity<?> availableQuizzes(Authentication authentication) {
        if (!isStudent(authentication)) return unauthorized();

        Student student = studentRepo.findWithAdminBySapId(authentication.getName()).orElse(null);
        if (student == null || student.getAdminId() == null) return ResponseEntity.notFound().build();

        List<QuizSummary> quizzes = quizRepository
                .findByAdmin_IdAndExpiresAtAfterOrderByExpiresAtAsc(student.getAdminId(), Instant.now())
                .stream()
                .map(quiz -> new QuizSummary(
                        quiz.getId(),
                        quiz.getDurationMinutes(),
                        quiz.getExpiresAt(),
                        quiz.getQuestions().size()))
                .toList();

        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/quizzes/{id}")
    public ResponseEntity<?> quiz(@PathVariable Long id, Authentication authentication) {
        if (!isStudent(authentication)) return unauthorized();

        Student student = studentRepo.findWithAdminBySapId(authentication.getName()).orElse(null);
        Quiz quiz = quizRepository.findWithQuestionsById(id).orElse(null);
        if (student == null || student.getAdminId() == null || quiz == null
                || quiz.getAdmin() == null || !student.getAdminId().equals(quiz.getAdmin().getId())
                || !quiz.getExpiresAt().isAfter(Instant.now())) {
            return ResponseEntity.notFound().build();
        }

        List<QuestionPrompt> questions = quiz.getQuestions().stream()
                .map(question -> new QuestionPrompt(
                        question.getId(),
                        question.getQuestion(),
                        List.of(question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD())))
                .toList();
        return ResponseEntity.ok(new QuizDetails(quiz.getId(), quiz.getDurationMinutes(), quiz.getExpiresAt(), questions));
    }

    private boolean isStudent(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_STUDENT"::equals);
    }

    private ResponseEntity<Void> unauthorized() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    public record StudentProfile(String sapId, String name, String adminId, String profileImageUrl) {}

    public record QuizSummary(Long id, Integer durationMinutes, Instant expiresAt, int questionCount) {}

    public record QuizDetails(Long id, Integer durationMinutes, Instant expiresAt, List<QuestionPrompt> questions) {}

    public record QuestionPrompt(Long id, String question, List<String> options) {}

    public record SubmitRequest(Map<Long, Integer> answers) {}

    public record ExamResult(Long attemptId, int score, int total, int percentage) {}

    public record AttemptResult(Long attemptId, Long quizId, int score, int total, int percentage, Instant submittedAt) {}
}
