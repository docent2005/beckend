package com.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // або використайте свою строку

    // Генерація токена
    public String generateToken(String email) {
        // 1 день
        long expirationMs = 86400000;

        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
        System.out.println("Токен готовий" + token);
        return token;
    }

    // Витягує email (subject) з токена
    public String extractEmail(String token) {
        System.out.println(token + "- це токен");
        return parseClaims(token).getBody().getSubject();
    }

    // Перевірка, чи токен валідний
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }
}
