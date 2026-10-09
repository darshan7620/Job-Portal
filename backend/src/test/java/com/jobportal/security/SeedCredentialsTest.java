package com.jobportal.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeedCredentialsTest {

    private static final String HR_HASH = "$2a$10$XQQ041ckd8zeW6y7XtbkzOV8pnw68FO1K76zCfXZjcZk/ui9gMkJm";
    private static final String CANDIDATE_HASH = "$2a$10$BR9RtugQc/Rkwybh7XRgq.YGU6B42EvFScr0hE/.6G3OdTnGv6Ome";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void seededHashesMatchDocumentedPasswords() {
        assertThat(passwordEncoder.matches("Hr@12345", HR_HASH)).isTrue();
        assertThat(passwordEncoder.matches("Candidate@123", CANDIDATE_HASH)).isTrue();
    }

    @Test
    void wrongPasswordDoesNotMatchSeededHash() {
        assertThat(passwordEncoder.matches("not-the-password", HR_HASH)).isFalse();
    }
}
