package com.jobportal.controller;

import com.jobportal.config.JwtProperties;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    private String registerBody(String username, String email, String role) {
        String rolePart = role == null ? "" : ",\"role\":\"" + role + "\"";
        return "{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"username\":\"" + username
                + "\",\"password\":\"Secret@123\",\"phone\":\"+10000000009\"" + rolePart + "}";
    }

    private String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher matcher = TOKEN_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("No token in login response: " + body);
        }
        return matcher.group(1);
    }

    private void createUser(String username, String email, String rawPassword, Role role) {
        User user = new User();
        user.setFullName("Seeded " + role);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    @Test
    void registerCreatesCandidateAndIgnoresPrivilegedRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("eve", "eve@example.com", "HR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("eve"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("mallory", "mallory@example.com", "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CANDIDATE"));
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("alice", "alice@example.com", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("alice", "Secret@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("CANDIDATE"));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        createUser("bob", "bob@example.com", "Secret@123", Role.CANDIDATE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("bob", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void loginWithUnknownUserReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ghost", "Secret@123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hrCanLoginByUsernameOrEmail() throws Exception {
        createUser("hrmanager", "hr@example.com", "Hr@12345", Role.HR);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("hrmanager", "Hr@12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("HR"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("hr@example.com", "Hr@12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("HR"));
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));
    }

    @Test
    void meWithValidTokenReturnsProfile() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("carol", "carol@example.com", null)))
                .andExpect(status().isCreated());
        String token = login("carol", "Secret@123");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));
    }

    @Test
    void meWithMalformedTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithExpiredTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("dave", "dave@example.com", null)))
                .andExpect(status().isCreated());

        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        String expired = Jwts.builder()
                .subject("dave")
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidateCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("erin", "erin@example.com", null)))
                .andExpect(status().isCreated());
        String token = login("erin", "Secret@123");

        mockMvc.perform(get("/api/admin/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"));
    }

    @Test
    void candidateCannotCreateJobPosting() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("frank", "frank@example.com", null)))
                .andExpect(status().isCreated());
        String token = login("frank", "Secret@123");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/overview"))
                .andExpect(status().isUnauthorized());
    }
}
