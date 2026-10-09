package com.jobportal.dto;

import com.jobportal.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class UserDtos {

    private UserDtos() {
    }

    public static class RegisterCandidateRequest {

        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Invalid email format")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        private String password;

        private String phone;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String username;
        private String phone;
        private Role role;
        private Boolean active;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}
