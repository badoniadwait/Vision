package com.project.vision.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.user.name:admin}")
    private String username;

    @Value("${spring.security.user.password:admin123}")
    private String password;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner seedAdmin(AdminRepo adminRepo, PasswordEncoder encoder) {
        return args -> {
            if (adminRepo.findByEmail(username).isEmpty() && adminRepo.findByName(username).isEmpty()) {
                Admin admin = new Admin();
                admin.setId(UUID.randomUUID().toString());
                admin.setName(username);
                admin.setEmail(username);
                admin.setPassword(encoder.encode(password));
                adminRepo.save(admin);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/admin/login", "/admin/signup", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {
                })
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/admin/login?error")
                        .permitAll());
        return http.build();
    }
}
