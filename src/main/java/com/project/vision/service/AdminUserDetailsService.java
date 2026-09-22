package com.project.vision.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepo adminRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Admin> adminOpt = adminRepo.findByEmail(username);
        if (adminOpt.isEmpty()) {
            adminOpt = adminRepo.findByName(username);
        }
        Admin admin = adminOpt.orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
        String principal = admin.getEmail() != null && !admin.getEmail().isBlank() ? admin.getEmail()
                : (admin.getName() != null && !admin.getName().isBlank() ? admin.getName() : username);
        return User.withUsername(principal)
                .password(admin.getPassword())
                .roles("ADMIN")
                .build();
    }
}
