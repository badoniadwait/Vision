package com.project.vision.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.project.vision.dto.StudentDto;
import com.project.vision.entity.Student;
import com.project.vision.service.StudentService;

@Controller 
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/register-student")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "registerStudent";
    }

    @PostMapping("/register-student")
    public String registerStudent(StudentDto student) throws IOException {
        studentService.registerStudent(student);
        return "redirect:/";
    }

}
