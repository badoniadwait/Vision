package com.project.vision.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.vision.entity.ExamAttempt;

public interface ExamAttemptRepo extends JpaRepository<ExamAttempt, Long> {
    @Query("select a from ExamAttempt a join fetch a.quiz where a.student.sap_id = :sapId order by a.submittedAt desc")
    List<ExamAttempt> findByStudentSapIdOrderBySubmittedAtDesc(@Param("sapId") String sapId);
}
