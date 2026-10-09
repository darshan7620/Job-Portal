package com.jobportal.service;

import com.jobportal.dto.UserDtos;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenOperationException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDtos.UserResponse registerCandidate(UserDtos.RegisterCandidateRequest request) {
        String username = trimToNull(request.getUsername());
        String email = trimToNull(request.getEmail());
        String fullName = trimToNull(request.getFullName());

        if (username == null || email == null || fullName == null) {
            throw new BadRequestException("Full name, email, and username are required");
        }
        email = email.toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setRole(Role.CANDIDATE);
        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDtos.UserResponse getById(Long id) {
        return toResponse(requireUser(id));
    }

    @Transactional(readOnly = true)
    public User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User requireCandidate(Long id) {
        User user = requireUser(id);
        if (user.getRole() != Role.CANDIDATE) {
            throw new ForbiddenOperationException("This action is only allowed for candidates");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User requireHr(Long id) {
        User user = requireUser(id);
        if (user.getRole() != Role.HR) {
            throw new ForbiddenOperationException("This action is only allowed for HR users");
        }
        return user;
    }

    private UserDtos.UserResponse toResponse(User user) {
        UserDtos.UserResponse response = new UserDtos.UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        return response;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
