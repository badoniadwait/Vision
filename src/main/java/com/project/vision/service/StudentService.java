package com.project.vision.service;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.project.vision.dto.StudentDto;
import com.project.vision.entity.Student;
import com.project.vision.repo.StudentRepo;

@Service
public class StudentService {

    private final StudentRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final ImageUploadService imageUploadService;

    public StudentService(StudentRepo repo, PasswordEncoder passwordEncoder, ImageUploadService imageUploadService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.imageUploadService = imageUploadService;
    }

    @Transactional
    public void registerStudent(StudentDto studentDto) throws IOException {
        if (studentDto == null
                || !StringUtils.hasText(studentDto.getSapId())
                || !StringUtils.hasText(studentDto.getName())
                || !StringUtils.hasText(studentDto.getPassword())) {
            throw new IllegalArgumentException("SAP ID, name, and password are required.");
        }
        if (studentDto.getReferencePhotoPath() == null || studentDto.getReferencePhotoPath().isEmpty()) {
            throw new IllegalArgumentException("A reference photo is required.");
        }

        if (repo.existsById(studentDto.getSapId())) {
            throw new IllegalArgumentException("A student with this SAP ID is already registered.");
        }

        String imageUrl = imageUploadService.uploadToSupabase(studentDto.getReferencePhotoPath());

        Student student = new Student();
        student.setSapId(studentDto.getSapId().trim());
        student.setName(studentDto.getName().trim());
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        student.setReferencePhotoPath(imageUrl);
        repo.save(student);
    }
}
