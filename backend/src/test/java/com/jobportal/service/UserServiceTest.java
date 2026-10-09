package com.jobportal.service;

import com.jobportal.dto.UserDtos;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenOperationException;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDtos.RegisterCandidateRequest request(String username, String email) {
        UserDtos.RegisterCandidateRequest request = new UserDtos.RegisterCandidateRequest();
        request.setFullName("Test Candidate");
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("Secret@123");
        return request;
    }

    @Test
    void registerCandidateHashesPasswordAndAssignsCandidateRole() {
        UserDtos.UserResponse response = userService.registerCandidate(request("alice", "Alice@Example.com"));

        assertThat(response.getId()).isNotNull();
        assertThat(response.getRole()).isEqualTo(Role.CANDIDATE);
        assertThat(response.getEmail()).isEqualTo("alice@example.com");

        User saved = userRepository.findById(response.getId()).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Secret@123");
        assertThat(saved.getPasswordHash()).startsWith("$2");
    }

    @Test
    void registerCandidateRejectsDuplicateUsername() {
        userService.registerCandidate(request("bob", "bob@example.com"));

        assertThatThrownBy(() -> userService.registerCandidate(request("bob", "bob2@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void requireHrRejectsCandidate() {
        UserDtos.UserResponse response = userService.registerCandidate(request("carol", "carol@example.com"));

        assertThatThrownBy(() -> userService.requireHr(response.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
