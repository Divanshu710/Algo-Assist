package com.codingapp.userservice.security;

import com.codingapp.userservice.model.UserTier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // 1. Generate a new JWT Token
    public String generateToken(UUID userId, String username, UserTier userTier) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("userTier", userTier.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // 2. Validate Token (Checks signature & expiration)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token signature invalid or expired
        }
    }

    // 3. Extract all claims (data payload) from Token
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 4. Extract User ID from Token
    public UUID getUserIdFromToken(String token) {
        String subject = getClaimsFromToken(token).getSubject();
        return UUID.fromString(subject);
    }

    // 5. Extract User Tier from Token
    public UserTier getUserTierFromToken(String token) {
        String tierStr = getClaimsFromToken(token).get("userTier", String.class);
        return UserTier.valueOf(tierStr);
    }
}