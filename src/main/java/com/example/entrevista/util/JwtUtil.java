package com.example.entrevista.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String username, String role, Long userId, String nombre, String apellidoPaterno, String apellidoMaterno) {
        Map<String, Object> claims = new HashMap<>();
        // Add "ROLE_" prefix if it doesn't exist already
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("nombre", nombre);
        claims.put("apellidoPaterno", apellidoPaterno);
        claims.put("apellidoMaterno", apellidoMaterno);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Mantener el método original para compatibilidad
    public String generateToken(String username, String role) {
        return generateToken(username, role, null, null, null, null);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Métodos para extraer la nueva información
    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        return userId != null ? ((Number) userId).longValue() : null;
    }

    public String extractNombre(String token) {
        return (String) extractAllClaims(token).get("nombre");
    }

    public String extractApellidoPaterno(String token) {
        return (String) extractAllClaims(token).get("apellidoPaterno");
    }

    public String extractApellidoMaterno(String token) {
        return (String) extractAllClaims(token).get("apellidoMaterno");
    }
}

