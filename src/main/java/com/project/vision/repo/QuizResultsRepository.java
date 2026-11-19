package com.project.vision.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.vision.entity.QuizResults;

public interface QuizResultsRepository extends JpaRepository<QuizResults, Long> {

    List<QuizResults> findByStudentId(String studentId);
}