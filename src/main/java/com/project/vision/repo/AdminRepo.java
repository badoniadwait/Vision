package com.project.vision.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.vision.entity.Admin;

public interface AdminRepo extends JpaRepository<Admin, String> {
    Optional<Admin> findByName(String name);

    Optional<Admin> findByEmail(String email);
}
