package com.project.vision.repo;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.vision.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = "questions")
    List<Quiz> findByAdmin_IdOrderByCreatedAtDesc(String adminId);

    @EntityGraph(attributePaths = "questions")
    Optional<Quiz> findByIdAndAdmin_Id(Long id, String adminId);

    @EntityGraph(attributePaths = "questions")
    List<Quiz> findByExpiresAtAfterOrderByExpiresAtAsc(Instant now);

    @EntityGraph(attributePaths = "questions")
    List<Quiz> findByAdmin_IdAndExpiresAtAfterOrderByExpiresAtAsc(String adminId, Instant now);

    @EntityGraph(attributePaths = "questions")
    @Query("select q from Quiz q left join fetch q.questions left join fetch q.admin where q.id = :id")
    Optional<Quiz> findWithQuestionsById(@Param("id") Long id);
}
