package com.project.vision.security;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import com.project.vision.entity.Admin;
import com.project.vision.repo.AdminOAuthAccountRepo;
import com.project.vision.repo.AdminRepo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GitHubOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    public static final String PENDING_REGISTRATION = GitHubOAuth2SuccessHandler.class.getName() + ".pending";
    private final AdminOAuthAccountRepo oauthRepo;
    private final AdminRepo adminRepo;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public GitHubOAuth2SuccessHandler(AdminOAuthAccountRepo oauthRepo, AdminRepo adminRepo) {
        this.oauthRepo = oauthRepo;
        this.adminRepo = adminRepo;
    }

    // @Override
    // public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    //         org.springframework.security.core.Authentication authentication) throws IOException, ServletException {
    //     OAuth2User user = (OAuth2User) authentication.getPrincipal();
    //     String githubId = String.valueOf(((Number) user.getAttribute("id")).longValue());
    //     var linked = oauthRepo.findByProviderAndProviderUserId("GITHUB", githubId);
    //     if (linked.isPresent()) {
    //         authenticate(request, response, linked.get().getAdmin());
    //         response.sendRedirect("/");
    //         return;
    //     }
    //     String email = user.getAttribute("verified_primary_email");
    //     if (adminRepo.findByEmailIgnoreCase(email).isPresent()) {
    //         response.sendRedirect("/admin/signup?githubEmailConflict");
    //         return;
    //     }
    //     String name = user.getAttribute("name");
    //     String login = user.getAttribute("login");
    //     request.getSession(true).setAttribute(PENDING_REGISTRATION,
    //             new PendingGitHubRegistration(githubId, email, name == null || name.isBlank() ? login : name));
    //     response.sendRedirect("/github/register");
    // }
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {

        System.out.println("\n========== GITHUB SUCCESS HANDLER ==========");

        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        String githubId = String.valueOf(
                ((Number) user.getAttribute("id")).longValue()
        );

        String email = user.getAttribute("verified_primary_email");

        System.out.println("GitHub ID: " + githubId);
        System.out.println("Verified email: " + email);

        var linked = oauthRepo.findByProviderAndProviderUserId(
                "GITHUB", githubId
        );

        System.out.println("Linked account exists: " + linked.isPresent());

        if (linked.isPresent()) {
            System.out.println("BRANCH: Existing GitHub account");
            authenticate(request, response, linked.get().getAdmin());
            response.sendRedirect("/");
            return;
        }

        System.out.println("Checking existing admin email...");

        if (adminRepo.findByEmailIgnoreCase(email).isPresent()) {
            System.out.println("BRANCH: Email already exists");

            // Clear the authenticated GitHub security context
            SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
            SecurityContextHolder.clearContext();

            // Remove the authenticated context from the HTTP session
            contextRepository.saveContext(emptyContext, request, response);

            response.sendRedirect("/admin/signup?githubEmailConflict");
            return;
        }

        System.out.println("BRANCH: New GitHub account");

        String name = user.getAttribute("name");
        String login = user.getAttribute("login");

        request.getSession(true).setAttribute(
                PENDING_REGISTRATION,
                new PendingGitHubRegistration(
                        githubId,
                        email,
                        name == null || name.isBlank() ? login : name
                )
        );

        System.out.println("Pending registration saved in session");
        System.out.println("Redirecting to /github/register");
        System.out.println("============================================\n");

        response.sendRedirect("/github/register");
    }

    public void authenticate(HttpServletRequest request, HttpServletResponse response, Admin admin) {
        String principal = admin.getEmail() == null || admin.getEmail().isBlank() ? admin.getName() : admin.getEmail();
        var token = new UsernamePasswordAuthenticationToken(principal, null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }

    public static final class PendingGitHubRegistration implements java.io.Serializable {

        private final String githubId;
        private final String email;
        private final String suggestedName;

        public PendingGitHubRegistration(String githubId, String email, String suggestedName) {
            this.githubId = githubId;
            this.email = email;
            this.suggestedName = suggestedName;
        }

        public String getGithubId() {
            return githubId;
        }

        public String getEmail() {
            return email;
        }

        public String getSuggestedName() {
            return suggestedName;
        }
    }
}
