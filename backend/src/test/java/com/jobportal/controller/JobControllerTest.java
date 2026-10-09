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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JobControllerTest {

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

    @BeforeEach
    void setUp() throws Exception {
        createUser("hr1", "hr1@example.com", Role.HR);
        createUser("hr2", "hr2@example.com", Role.HR);
        createUser("cand1", "cand1@example.com", Role.CANDIDATE);
        hrToken = login("hr1");
        otherHrToken = login("hr2");
        candidateToken = login("cand1");
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

    private String jobBody(String title, String status, String type) {
        String typePart = type == null ? "" : ",\"employmentType\":\"" + type + "\"";
        String statusPart = status == null ? "" : ",\"status\":\"" + status + "\"";
        return "{\"title\":\"" + title + "\",\"company\":\"Acme\",\"description\":\"desc\""
                + typePart + statusPart + "}";
    }

    private long createJob(String token, String title, String status, String type) throws Exception {
        String body = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody(title, status, type)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = ID_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("No id in create response: " + body);
        }
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void browseReturnsOnlyPublishedJobs() throws Exception {
        createJob(hrToken, "Draft Job", "DRAFT", null);
        createJob(hrToken, "Live Job", "PUBLISHED", null);

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Live Job"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchFiltersByKeywordAndType() throws Exception {
        createJob(hrToken, "Java Developer", "PUBLISHED", "FULL_TIME");
        createJob(hrToken, "Java Intern", "PUBLISHED", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Java")
                        .param("type", "INTERNSHIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Java Intern"));
    }

    @Test
    void getReturnsPublishedJobAndHidesDraft() throws Exception {
        long liveId = createJob(hrToken, "Live Job", "PUBLISHED", null);
        long draftId = createJob(hrToken, "Draft Job", "DRAFT", null);

        mockMvc.perform(get("/api/jobs/" + liveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Live Job"));

        mockMvc.perform(get("/api/jobs/" + draftId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequiresHrRole() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody("Job", "DRAFT", null)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody("Job", "DRAFT", null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRejectsNegativeSalary() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Job\",\"company\":\"Acme\",\"description\":\"desc\",\"salaryMin\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("salaryMin")));
    }

    @Test
    void createRejectsInvalidSalaryRange() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Job\",\"company\":\"Acme\",\"description\":\"desc\","
                                + "\"salaryMin\":90000,\"salaryMax\":50000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Minimum salary cannot be greater than maximum salary"));
    }

    @Test
    void updateRejectsNonOwnerAndAllowsOwner() throws Exception {
        long jobId = createJob(hrToken, "Owner Job", "DRAFT", null);

        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherHrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hijacked\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void deleteRequiresOwnership() throws Exception {
        long jobId = createJob(hrToken, "Temp Job", "DRAFT", null);

        mockMvc.perform(delete("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherHrToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void hrListsOwnJobsAndCannotReadOthers() throws Exception {
        createJob(hrToken, "My Job", "DRAFT", null);
        long otherJobId = createJob(otherHrToken, "Their Job", "DRAFT", null);

        mockMvc.perform(get("/api/hr/jobs").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("My Job"));

        mockMvc.perform(get("/api/hr/jobs/" + otherJobId)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }
}
