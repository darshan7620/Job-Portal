package com.jobportal.security;

import com.jobportal.config.JwtProperties;
import com.jobportal.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    private AppUserDetails user() {
        return new AppUserDetails(1L, "alice", "irrelevant-hash", Role.CANDIDATE, true);
    }

    @Test
    void generateAndValidateToken() {
        String token = jwtService.generateToken(user());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token, user())).isTrue();
        assertThat(jwtService.parseClaims(token).get("role")).isEqualTo("CANDIDATE");
    }

    @Test
    void tokenForDifferentUserIsInvalid() {
        String token = jwtService.generateToken(user());
        AppUserDetails other = new AppUserDetails(2L, "bob", "hash", Role.HR, true);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        String expired = Jwts.builder()
                .subject("alice")
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        assertThat(jwtService.isTokenValid(expired, user())).isFalse();
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(user());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tampered, user())).isFalse();
        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void missingSecretFailsFast() {
        JwtProperties missing = new JwtProperties();
        missing.setSecret("   ");

        assertThatThrownBy(() -> new JwtService(missing))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void shortSecretFailsFast() {
        JwtProperties shortSecret = new JwtProperties();
        shortSecret.setSecret("too-short");

        assertThatThrownBy(() -> new JwtService(shortSecret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
