package com.project.vision.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.vision.security.GitHubOAuth2SuccessHandler;
import com.project.vision.security.GitHubOAuth2SuccessHandler.PendingGitHubRegistration;
import com.project.vision.service.GitHubRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class GitHubRegistrationController {
    private final GitHubRegistrationService registrationService;
    private final GitHubOAuth2SuccessHandler successHandler;

    public GitHubRegistrationController(GitHubRegistrationService registrationService,
            GitHubOAuth2SuccessHandler successHandler) {
        this.registrationService = registrationService;
        this.successHandler = successHandler;
    }

    @GetMapping("/github/register")
    public String form(HttpServletRequest request, Model model) {
        PendingGitHubRegistration pending = pending(request);
        if (pending == null) return "redirect:/admin/signup";
        model.addAttribute("githubEmail", pending.getEmail());
        model.addAttribute("suggestedName", pending.getSuggestedName());
        return "githubRegistration";
    }

    @PostMapping("/github/register")
    public String complete(@RequestParam String name, @RequestParam String subject,
            HttpServletRequest request, HttpServletResponse response) {
        PendingGitHubRegistration pending = pending(request);
        if (pending == null) return "redirect:/admin/signup";
        if (!StringUtils.hasText(name) || !StringUtils.hasText(subject)) return "redirect:/github/register?error";
        try {
            var admin = registrationService.complete(name, subject, pending.getEmail(), pending.getGithubId());
            request.getSession().removeAttribute(GitHubOAuth2SuccessHandler.PENDING_REGISTRATION);
            successHandler.authenticate(request, response, admin);
            return "redirect:/";
        } catch (IllegalStateException exception) {
            request.getSession().removeAttribute(GitHubOAuth2SuccessHandler.PENDING_REGISTRATION);
            return "redirect:/admin/signup?githubEmailConflict";
        }
    }

    private PendingGitHubRegistration pending(HttpServletRequest request) {
        if (request.getSession(false) == null) return null;
        Object value = request.getSession(false).getAttribute(GitHubOAuth2SuccessHandler.PENDING_REGISTRATION);
        return value instanceof PendingGitHubRegistration pending ? pending : null;
    }
}
