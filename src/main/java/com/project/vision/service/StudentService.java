package com.project.vision.service;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.project.vision.dto.StudentDto;
import com.project.vision.entity.Admin;
import com.project.vision.entity.Student;
import com.project.vision.model.StudentLoginRequest;
import com.project.vision.model.StudentLoginResponse;
import com.project.vision.model.StudentRes;
import com.project.vision.repo.AdminRepo;
import com.project.vision.repo.StudentRepo;
import com.project.vision.security.StudentJwtService;

@Service
public class StudentService {

    private final StudentRepo repo;
    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final ImageUploadService imageUploadService;
    private final StudentJwtService jwtService;

    public StudentService(
            StudentRepo repo,
            AdminRepo adminRepo,
            PasswordEncoder passwordEncoder,
            ImageUploadService imageUploadService,
            StudentJwtService jwtService) {
        this.repo = repo;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.imageUploadService = imageUploadService;
        this.jwtService = jwtService;
    }

    @Transactional
    public void registerStudent(StudentDto studentDto, String adminUsername) throws IOException {
        if (studentDto == null
                || !StringUtils.hasText(studentDto.getSapId())
                || !StringUtils.hasText(studentDto.getName())
                || !StringUtils.hasText(studentDto.getPassword())) {
            throw new IllegalArgumentException("SAP ID, name, and password are required.");
        }
        if (!StringUtils.hasText(adminUsername)) {
            throw new IllegalArgumentException("An authenticated admin is required to register a student.");
        }
        String sapId = studentDto.getSapId().trim();
        if (studentDto.getReferencePhotoPath() == null || studentDto.getReferencePhotoPath().isEmpty()) {
            throw new IllegalArgumentException("A reference photo is required.");
        }

        Admin admin = adminRepo.findByEmail(adminUsername)
                .or(() -> adminRepo.findByName(adminUsername))
                .orElseThrow(() -> new IllegalArgumentException("The authenticated admin account was not found."));
        if (repo.existsBySapIdAndAdminId(sapId, admin.getId())) {
            throw new IllegalArgumentException("This student is already registered under the specified admin.");
        }

        String imageUrl = imageUploadService.uploadToSupabase(studentDto.getReferencePhotoPath());

        Student student = new Student();
        student.setAdmin(admin);
        student.setSapId(sapId);
        student.setName(studentDto.getName().trim());
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        student.setReferencePhotoPath(imageUrl);

        repo.save(student);
    }

    @Transactional(readOnly = true)
    public StudentLoginResponse studentLoginService(StudentLoginRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getSapId())
                || !StringUtils.hasText(request.getPassword())) {
            return loginFailure("SAP ID and password are required.");
        }

        String sapId = request.getSapId().trim();
        Student student = repo.findById(sapId).orElse(null);
        if (student == null
                || !StringUtils.hasText(student.getPassword())
                || !passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            // Use the same message for an unknown SAP ID and a wrong password.
            return loginFailure("Invalid SAP ID or password.");
        }

        String imgUrl = repo.findById(sapId).map(Student::getReferencePhotoPath).orElse(null);

        System.out.println(imgUrl);
        
        StudentRes studentResponse = new StudentRes();
        studentResponse.setSapId(student.getSapId());
        studentResponse.setName(student.getName());
        studentResponse.setAdminId(student.getAdminId());

        StudentLoginResponse response = new StudentLoginResponse();
        response.setSuccess(true);
        response.setMessage("Login successful.");
        response.setStudent(studentResponse);
        response.setToken(jwtService.generateToken(student.getSapId()));
        response.setImgUrl(imgUrl);
        return response;
    }

    private StudentLoginResponse loginFailure(String message) {
        StudentLoginResponse response = new StudentLoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStudent(null);
        response.setToken(null);
        return response;
    }
}
