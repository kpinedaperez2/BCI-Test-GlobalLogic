package com.local.bci.infrastructure.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service responsible for generating, validating, and parsing JWT tokens.
 * <p>
 * Uses a secret key and expiration configuration to create signed tokens,
 * extract the subject (typically the user's email), and validate token integrity.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Generates a JWT token for a given subject.
     *
     * @param subject the subject to include in the token (usually user email)
     * @return a signed JWT token string
     */
    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Extracts the subject from a JWT token.
     *
     * @param token the JWT token
     * @return the subject (email) if token is valid; otherwise null
     */
    public String getSubject(String token) {
        try {
            return Jwts.parser().setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token
     * @return true if the token is valid; false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
