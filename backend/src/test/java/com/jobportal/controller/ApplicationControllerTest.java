package com.jobportal.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationControllerTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String hrToken;
    private String otherHrToken;
    private String candidateToken;
    private String otherCandidateToken;

    @BeforeEach
    void setUp() throws Exception {
        createUser("hr1", "hr1@example.com", Role.HR);
        createUser("hr2", "hr2@example.com", Role.HR);
        createUser("cand1", "cand1@example.com", Role.CANDIDATE);
        createUser("cand2", "cand2@example.com", Role.CANDIDATE);
        hrToken = login("hr1");
        otherHrToken = login("hr2");
        candidateToken = login("cand1");
        otherCandidateToken = login("cand2");
    }

    private void createUser(String username, String email, Role role) {
        User user = new User();
        user.setFullName(username);
        user.setUsername(username);
        user.setEmail(email);
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

    private long createJob(String token, String status) throws Exception {
        String body = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Backend Engineer\",\"company\":\"Acme\",\"description\":\"desc\","
                                + "\"status\":\"" + status + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(body);
    }

    private long extractId(String body) {
        Matcher matcher = ID_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("No id in response: " + body);
        }
        return Long.parseLong(matcher.group(1));
    }

    private long applyForJob(String token, long jobId, int expectedStatus) throws Exception {
        String body = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"I am a great fit\"}"))
                .andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        return extractId(body);
    }

    @Test
    void candidateCanApplyForPublishedJob() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"Hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void duplicateApplicationIsRejected() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("You have already applied for this job"));
    }

    @Test
    void cannotApplyForDraftJob() throws Exception {
        long jobId = createJob(hrToken, "DRAFT");

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void applyRequiresCandidateRole() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidateCanListAndWithdrawOwnApplication() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        long applicationId = applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(get("/api/candidates/applications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobId").value(jobId));

        mockMvc.perform(delete("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void candidateCannotWithdrawSomeoneElsesApplication() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        long applicationId = applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(delete("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getApplicationEnforcesOwnership() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        long applicationId = applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + otherHrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanListApplicantsForOwnJob() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(get("/api/hr/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/hr/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + otherHrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanAdvanceApplicationStatus() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        long applicationId = applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(patch("/api/hr/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void invalidStatusTransitionIsRejected() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        long applicationId = applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(patch("/api/hr/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"HIRED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void candidateCannotChangeApplicationStatus() throws Exception {
        long jobId = createJob(hrToken, "PUBLISHED");
        long applicationId = applyForJob(candidateToken, jobId, 201);

        mockMvc.perform(patch("/api/hr/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"HIRED\"}"))
                .andExpect(status().isForbidden());
    }
}
