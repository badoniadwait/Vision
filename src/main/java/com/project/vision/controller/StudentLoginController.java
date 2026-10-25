package com.project.vision.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.project.vision.model.StudentLoginRequest;
import com.project.vision.model.StudentLoginResponse;
import com.project.vision.service.StudentService;

@RestController
public class StudentLoginController {

    private final StudentService studentService;

    public StudentLoginController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/student-login")
    public ResponseEntity<StudentLoginResponse> login(@RequestBody StudentLoginRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getSapId())
                || !StringUtils.hasText(request.getPassword())) {
            StudentLoginResponse response = new StudentLoginResponse();
            response.setSuccess(false);
            response.setMessage("SAP ID and password are required.");
            response.setToken(null);
            response.setStudent(null);
            return ResponseEntity.badRequest().body(response);
        }

        StudentLoginResponse response = studentService.studentLoginService(request);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}
