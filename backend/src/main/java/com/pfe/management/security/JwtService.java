package com.pfe.management.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret:dGhpcy1pcy1hLXZlcnktc2VjdXJlLWJhc2U2NC1zZWNyZXQta2V5LXRvLXByb3RlY3Qtand0LXRva2Vucy0yNTZiaXRz}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public String generateTokenForUser(String email, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", "ROLE_" + role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public boolean validateToken(String authToken) {
        try {
            extractClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.warn("Signature JWT invalide");
        } catch (MalformedJwtException ex) {
            log.warn("Format de jeton JWT invalide");
        } catch (ExpiredJwtException ex) {
            log.warn("Jeton JWT expiré");
        } catch (UnsupportedJwtException ex) {
            log.warn("Jeton JWT non supporté");
        } catch (IllegalArgumentException ex) {
            log.warn("Revendications JWT vides");
        }
        return false;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

