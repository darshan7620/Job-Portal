package com.jobportal.controller;

import com.jobportal.ai.AiMatcherService;
import com.jobportal.ai.InvalidAiResponseException;
import com.jobportal.ai.OllamaUnavailableException;
import com.jobportal.dto.AiDtos;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AiControllerTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final String VALID_BODY =
            "{\"skills\":[\"Java\",\"MySQL\"],\"jobDescription\":\"Java developer with Hibernate and MySQL.\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AiMatcherService aiMatcherService;

    private String candidateToken;
    private String hrToken;

    @BeforeEach
    void setUp() throws Exception {
        saveUser("cand1", Role.CANDIDATE);
        saveUser("hr1", Role.HR);
        candidateToken = login("cand1");
        hrToken = login("hr1");
    }

    private void saveUser(String username, Role role) {
        User user = new User();
        user.setFullName(username);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("Secret@123"));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Secret@123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = TOKEN_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("No token in login response: " + body);
        }
        return matcher.group(1);
    }

    private AiDtos.MatchResponse sampleResponse() {
        AiDtos.MatchResponse response = new AiDtos.MatchResponse();
        response.setMatchingSkills(List.of("Java", "MySQL"));
        response.setMissingSkills(List.of("Hibernate"));
        response.setImprovementSuggestions(List.of("Learn Hibernate ORM"));
        response.setSummary("Strong overlap on Java and MySQL.");
        response.setMatchScore(72);
        response.setMatchScoreNote(AiMatcherService.SCORE_NOTE);
        return response;
    }

    @Test
    void returnsStructuredMatchForCandidate() throws Exception {
        when(aiMatcherService.match(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchingSkills[0]").value("Java"))
                .andExpect(jsonPath("$.missingSkills[0]").value("Hibernate"))
                .andExpect(jsonPath("$.matchScore").value(72))
                .andExpect(jsonPath("$.matchScoreNote").isNotEmpty());
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/ai/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNonCandidateRoles() throws Exception {
        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsEmptySkillList() throws Exception {
        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":[],\"jobDescription\":\"Java role\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankJobDescription() throws Exception {
        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":[\"Java\"],\"jobDescription\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOversizedJobDescription() throws Exception {
        String body = "{\"skills\":[\"Java\"],\"jobDescription\":\"" + "x".repeat(5000) + "\"}";

        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceUnavailableWhenOllamaIsDown() throws Exception {
        when(aiMatcherService.match(any())).thenThrow(new OllamaUnavailableException("down"));

        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void returnsBadGatewayForUnparseableModelOutput() throws Exception {
        when(aiMatcherService.match(any())).thenThrow(new InvalidAiResponseException("bad"));

        mockMvc.perform(post("/api/ai/match")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadGateway());
    }
}
