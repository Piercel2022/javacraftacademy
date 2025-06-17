package com.javacraftacademy.userservice.service;

import com.javacraftacademy.userservice.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    
     private static final long SUPER_ADMIN_ACCESS_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000; // 30 jours
     private static final long SUPER_ADMIN_REFRESH_TOKEN_EXPIRATION = 90L * 24 * 60 * 60 * 1000; // 90 jours


    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private int refreshTokenExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId());
    }

    public String generateTokenFromUserId(Long userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpirationInMs, ChronoUnit.MILLIS);

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(refreshTokenExpirationInMs, ChronoUnit.MILLIS);

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        }
        return false;
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public int getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }

    public int getRefreshTokenExpirationInMs() {
        return refreshTokenExpirationInMs;
    }

    // Ajoutez ces méthodes à votre JwtService existant

/**
 * Génère un token d'accès avec une durée prolongée pour le super admin
 */
public String generateSuperAdminAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("email", user.getEmail());
    claims.put("username", user.getUsername());
    claims.put("roles", user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));
    claims.put("isFounder", true);
    claims.put("isSuperAdmin", true);
    claims.put("permissions", List.of("ALL"));
    
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + SUPER_ADMIN_ACCESS_TOKEN_EXPIRATION))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}

/**
 * Génère un refresh token avec une durée prolongée pour le super admin
 */
public String generateSuperAdminRefreshToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("email", user.getEmail());
    claims.put("isFounder", true);
    claims.put("tokenType", "refresh");
    
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + SUPER_ADMIN_REFRESH_TOKEN_EXPIRATION))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}

/**
 * Retourne la durée d'expiration des tokens super admin (30 jours)
 */
public Long getSuperAdminTokenExpiration() {
    return SUPER_ADMIN_ACCESS_TOKEN_EXPIRATION / 1000; // En secondes
}

/**
 * Vérifie si un token appartient au super admin
 */
public boolean isSuperAdminToken(String token) {
    try {
        Claims claims = extractAllClaims(token);
        return Boolean.TRUE.equals(claims.get("isFounder")) || 
               Boolean.TRUE.equals(claims.get("isSuperAdmin"));
    } catch (Exception e) {
        return false;
    }
}

/**
 * Extrait les permissions du token
 */
public List<String> getPermissionsFromToken(String token) {
    Claims claims = extractAllClaims(token);
    @SuppressWarnings("unchecked")
    List<String> permissions = (List<String>) claims.get("permissions");
    return permissions != null ? permissions : List.of();
 }
}
