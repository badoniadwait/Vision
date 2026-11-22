package com.project.vision.controller;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.project.vision.dto.StudentDto;
import com.project.vision.entity.Student;
import com.project.vision.model.FaceVerificationResponse;
import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;
import com.project.vision.service.FaceService;

import com.project.vision.service.StudentService;

@Controller
public class StudentRegistrationController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private FaceService faceService;

    @Autowired
    private AdminRepo adminRepo;

    @GetMapping("/register-student")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "registerStudent";
    }

    @PostMapping("/register-student")
    public String registerStudent(StudentDto student, Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/admin/login";
        }

        MultipartFile image = student.getReferencePhotoPath();
        byte[] bytes = image.getBytes();

        Mat img = Imgcodecs.imdecode(
                new MatOfByte(bytes),
                Imgcodecs.IMREAD_COLOR
        );
        FaceVerificationResponse res = faceService.detectFace(img);

        boolean verified = res.isVerified();

        if (!verified) {
            return "redirect:/admin/profile?faceDetectionFailed";
        }

        String username = authentication.getName();
        Admin admin = adminRepo.findByEmailIgnoreCase(username)
                .or(() -> adminRepo.findByName(username))
                .orElse(null);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsVerified())) {
            return "redirect:/admin/profile?verificationRequired";
        }

        studentService.registerStudent(student, username);
        return "redirect:/";
    }

}
