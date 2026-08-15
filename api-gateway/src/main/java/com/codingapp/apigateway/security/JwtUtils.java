package com.codingapp.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtils {

    private final SecretKey key;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 1. Validates the signature and expiration of the incoming token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 2. Extracts the Claims payload from the token.
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 3. Extracts userId (stored as the Subject in user-service).
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 4. Extracts userTier (stored as "userTier" claim).
     */
    public String getUserTier(String token) {
        return getClaims(token).get("userTier", String.class);
    }

    /**
     * 5. Extracts username (stored as "username" claim).
     */
    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }
}