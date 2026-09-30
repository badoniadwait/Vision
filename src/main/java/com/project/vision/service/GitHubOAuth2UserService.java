package com.project.vision.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubOAuth2UserService extends DefaultOAuth2UserService {
    private final RestClient restClient;

    public GitHubOAuth2UserService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(request);
        if (!"github".equals(request.getClientRegistration().getRegistrationId())) return user;

        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri("https://api.github.com/user/emails")
                    .headers(headers -> headers.setBearerAuth(request.getAccessToken().getTokenValue()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            String verifiedPrimaryEmail = emails == null ? null : emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.get("primary"))
                            && Boolean.TRUE.equals(email.get("verified")))
                    .map(email -> email.get("email"))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .findFirst().orElse(null);
            if (verifiedPrimaryEmail == null || !(user.getAttribute("id") instanceof Number)) {
                throw new IllegalArgumentException("GitHub did not provide a verified primary email and account ID.");
            }
            var attributes = new java.util.HashMap<String, Object>(user.getAttributes());
            attributes.put("verified_primary_email", verifiedPrimaryEmail.trim().toLowerCase(java.util.Locale.ROOT));
            return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                    user.getAuthorities(), attributes, "id");
        } catch (RuntimeException exception) {
            if (exception instanceof OAuth2AuthenticationException oauthException) throw oauthException;
            throw new OAuth2AuthenticationException(new OAuth2Error("github_identity_error"),
                    "Unable to retrieve a verified primary GitHub email.", exception);
        }
    }
}
