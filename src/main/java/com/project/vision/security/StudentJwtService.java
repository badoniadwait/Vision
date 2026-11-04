package com.project.vision.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class StudentJwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public StudentJwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (!StringUtils.hasText(base64Secret)) {
            throw new IllegalArgumentException("JWT signing secret must be configured.");
        }
        if (expirationMs <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive.");
        }

        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("JWT signing secret must be Base64 encoded and at least 256 bits.", ex);
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(String sapId) {
        return generateToken(sapId, "STUDENT");
    }

    public String generateToken(String subject, String role) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMs);
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public String extractSapId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (claims.getExpiration() == null || !StringUtils.hasText(claims.getSubject())) {
            throw new IllegalArgumentException("JWT is missing required claims.");
        }
        return claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object role = claims.get("role");
        return role instanceof String && StringUtils.hasText((String) role)
                ? (String) role
                : "STUDENT";
    }
}
