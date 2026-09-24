package com.project.vision.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.vision.entity.Student;

public interface StudentRepo extends JpaRepository<Student, String> {

}
