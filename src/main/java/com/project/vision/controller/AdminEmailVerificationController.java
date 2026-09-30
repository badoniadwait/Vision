package com.project.vision.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.vision.service.AdminEmailVerificationService;

@Controller
public class AdminEmailVerificationController {

    private final AdminEmailVerificationService verificationService;

    public AdminEmailVerificationController(AdminEmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/admin/verify-email")
    public String requestVerification(Authentication authentication) {
        try {
            verificationService.requestVerification(authentication.getName());
            return "redirect:/admin/profile?verificationEmailSent";

        } catch (IllegalStateException exception) {
            System.out.println("EMAIL VERIFICATION ERROR:");
            exception.printStackTrace();
            return "redirect:/admin/profile?verificationError";

        } catch (RuntimeException exception) {
            System.out.println("EMAIL SENDING FAILED:");
            exception.printStackTrace();
            return "redirect:/admin/profile?verificationError";
        }
    }

    @GetMapping("/admin/verify-email/confirm")
    public String confirm(@RequestParam(required = false) String token, Model model) {
        boolean verified;
        try {
            verified = verificationService.confirm(token);
        } catch (RuntimeException exception) {
            verified = false;
        }
        model.addAttribute("verified", verified);
        return "emailVerificationResult";
    }
}
