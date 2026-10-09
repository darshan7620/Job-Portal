package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public static class LoginRequest {

        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {

        private String token;
        private String tokenType;
        private long expiresIn;
        private UserDtos.UserResponse user;

        public LoginResponse(String token, String tokenType, long expiresIn, UserDtos.UserResponse user) {
            this.token = token;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public String getTokenType() {
            return tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public UserDtos.UserResponse getUser() {
            return user;
        }
    }
}
