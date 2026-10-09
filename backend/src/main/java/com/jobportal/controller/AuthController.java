package com.jobportal.controller;

import com.jobportal.dto.AuthDtos;
import com.jobportal.dto.UserDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.security.JwtService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDtos.UserResponse register(@Valid @RequestBody UserDtos.RegisterCandidateRequest request) {
        return userService.registerCandidate(request);
    }

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        return new AuthDtos.LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                userService.getById(principal.getId()));
    }

    @GetMapping("/me")
    public UserDtos.UserResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return userService.getById(principal.getId());
    }
}
