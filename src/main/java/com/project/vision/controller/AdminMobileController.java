package com.project.vision.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.vision.dto.QuizDto;
import com.project.vision.dto.StudentDto;
import com.project.vision.entity.Admin;
import com.project.vision.entity.Quiz;
import com.project.vision.repo.AdminRepo;
import com.project.vision.repo.QuizRepository;
import com.project.vision.repo.StudentRepo;
import com.project.vision.service.AdminEmailVerificationService;
import com.project.vision.service.QuizService;
import com.project.vision.service.StudentService;

@RestController
@RequestMapping("/api/admin")
public class AdminMobileController {

    private final AdminRepo adminRepo;
    private final StudentRepo studentRepo;
    private final QuizRepository quizRepository;
    private final QuizService quizService;
    private final StudentService studentService;
    private final AdminEmailVerificationService verificationService;

    public AdminMobileController(
            AdminRepo adminRepo,
            StudentRepo studentRepo,
            QuizRepository quizRepository,
            QuizService quizService,
            StudentService studentService,
            AdminEmailVerificationService verificationService) {
        this.adminRepo = adminRepo;
        this.studentRepo = studentRepo;
        this.quizRepository = quizRepository;
        this.quizService = quizService;
        this.studentService = studentService;
        this.verificationService = verificationService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        return ResponseEntity.ok(new AdminProfile(
                admin.getId(), admin.getName(), admin.getEmail(), admin.getSubject(),
                Boolean.TRUE.equals(admin.getIsVerified()), studentRepo.countByAdmin_Id(admin.getId())));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        List<AdminQuiz> quizzes = quizRepository.findByAdmin_IdOrderByCreatedAtDesc(admin.getId()).stream()
                .map(quiz -> new AdminQuiz(quiz.getId(), quiz.getDurationMinutes(), quiz.getCreatedAt(), quiz.getExpiresAt(), quiz.getQuestions().size()))
                .toList();
        return ResponseEntity.ok(new Dashboard(quizzes.size(), studentRepo.countByAdmin_Id(admin.getId()), quizzes));
    }

    @PostMapping("/quizzes")
    public ResponseEntity<?> createQuiz(@RequestBody QuizDto quizDto, Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        if (!Boolean.TRUE.equals(admin.getIsVerified())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorMessage("Verify your email before creating a quiz."));
        try {
            Quiz quiz = quizService.saveQuiz(quizDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new AdminQuiz(quiz.getId(), quiz.getDurationMinutes(), quiz.getCreatedAt(), quiz.getExpiresAt(), quiz.getQuestions().size()));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorMessage(exception.getMessage() == null ? "Invalid quiz." : exception.getMessage()));
        }
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id, Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        Quiz quiz = quizRepository.findByIdAndAdmin_Id(id, admin.getId()).orElse(null);
        if (quiz == null) return ResponseEntity.notFound().build();
        quizRepository.delete(quiz);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/quizzes/{id}")
    public ResponseEntity<?> quiz(@PathVariable Long id, Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        Quiz quiz = quizRepository.findByIdAndAdmin_Id(id, admin.getId()).orElse(null);
        if (quiz == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new QuizDetail(
                quiz.getId(), quiz.getDurationMinutes(), quiz.getCreatedAt(), quiz.getExpiresAt(),
                quiz.getQuestions().stream().map(question -> new QuizQuestion(
                        question.getQuestion(),
                        List.of(question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD()),
                        question.getAnswer())).toList()));
    }

    @PostMapping("/students")
    public ResponseEntity<?> registerStudent(
            @RequestPart("sapId") String sapId,
            @RequestPart("name") String name,
            @RequestPart("password") String password,
            @RequestPart("referencePhotoPath") MultipartFile referencePhotoPath,
            Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        if (!Boolean.TRUE.equals(admin.getIsVerified())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorMessage("Verify your email before registering a student."));
        StudentDto student = new StudentDto();
        student.setSapId(sapId);
        student.setName(name);
        student.setPassword(password);
        student.setReferencePhotoPath(referencePhotoPath);
        try {
            studentService.registerStudent(student, admin.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(new Message("Student registered successfully."));
        } catch (IOException | RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorMessage(exception.getMessage() == null ? "Unable to register student." : exception.getMessage()));
        }
    }

    @PostMapping("/profile/verify-email")
    public ResponseEntity<?> requestVerification(Authentication authentication) {
        Admin admin = admin(authentication);
        if (admin == null) return forbidden();
        try {
            verificationService.requestVerification(admin.getEmail());
            return ResponseEntity.ok(new Message("Verification email sent."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorMessage(exception.getMessage() == null ? "Unable to send verification email." : exception.getMessage()));
        }
    }

    private Admin admin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch("ROLE_ADMIN"::equals)) return null;
        return adminRepo.findByEmailIgnoreCase(authentication.getName()).orElse(null);
    }

    private ResponseEntity<Void> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    public record AdminProfile(String id, String name, String email, String subject, boolean verified, long studentCount) {}
    public record Dashboard(int quizCount, long studentCount, List<AdminQuiz> quizzes) {}
    public record AdminQuiz(Long id, Integer durationMinutes, java.time.Instant createdAt, java.time.Instant expiresAt, int questionCount) {}
    public record QuizDetail(Long id, Integer durationMinutes, java.time.Instant createdAt, java.time.Instant expiresAt, List<QuizQuestion> questions) {}
    public record QuizQuestion(String question, List<String> options, Integer answer) {}
    public record Message(String message) {}
    public record ErrorMessage(String message) {}
}
