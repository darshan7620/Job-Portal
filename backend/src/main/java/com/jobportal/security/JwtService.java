package com.jobportal.security;

import com.jobportal.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationMillis;
    private final String issuer;

    public JwtService(JwtProperties properties) {
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. Set the JWT_SECRET environment variable.");
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_SECRET_BYTES
                            + " bytes (256 bits) for HS256. Set a longer JWT_SECRET.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = properties.getExpirationMinutes() * 60_000L;
        this.issuer = properties.getIssuer();
    }

    public String generateToken(AppUserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000L;
    }
}
