package com.project.vision.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.vision.dto.AdminDto;
import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;

@Service
public class AdminService {

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin register(AdminDto dto) {
        Admin admin = new Admin();
        admin.setId(UUID.randomUUID().toString());


        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        return adminRepo.save(admin);
    }

    public Admin register(String username, String password, String email, String name, String subject) {
        AdminDto dto = new AdminDto();
        dto.setPassword(password);
        dto.setEmail(email);
        dto.setName(name);
        dto.setSubject(subject);
        return register(dto);
    }
    
    
}
    