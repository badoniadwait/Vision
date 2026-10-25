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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;

import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminRepo;
import com.project.vision.security.StudentJwtAuthenticationFilter;
import com.project.vision.security.StudentJwtService;

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
    @Order(1)
    public SecurityFilterChain mobileApiSecurityFilterChain(
            HttpSecurity http,
            StudentJwtService jwtService) throws Exception {
        StudentJwtAuthenticationFilter jwtAuthenticationFilter = new StudentJwtAuthenticationFilter(jwtService);
        AuthenticationEntryPoint unauthorizedEntryPoint = (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Unauthorized\"}");
        };

        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(unauthorizedEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/verify-face").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/api/verify-face", "/student-login", "/admin/login", "/admin/signup", "/admin/register", "/css/**", "/js/**", "/images/**").permitAll()
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
