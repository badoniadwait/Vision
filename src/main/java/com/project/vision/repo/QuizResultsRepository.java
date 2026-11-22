package com.project.vision.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.vision.entity.QuizResults;

public interface QuizResultsRepository extends JpaRepository<QuizResults, Long> {

    List<QuizResults> findByStudentId(String studentId);
    Optional<QuizResults> findByStudentIdAndQuizId(
        String studentId,
        String quizId
);
}