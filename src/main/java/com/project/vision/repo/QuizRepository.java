package com.project.vision.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.vision.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = "questions")
    List<Quiz> findByAdmin_IdOrderByCreatedAtDesc(String adminId);

    Optional<Quiz> findByIdAndAdmin_Id(Long id, String adminId);
}
