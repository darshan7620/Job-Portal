package com.jobportal.controller;

import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.JobStatus;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String candidateToken;
    private String hrToken;

    @BeforeEach
    void setUp() throws Exception {
        Long candidateId = saveUser("cand1", Role.CANDIDATE);
        Long hrId = saveUser("hr1", Role.HR);

        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setCompany("Acme");
        job.setDescription("Description");
        job.setStatus(JobStatus.PUBLISHED);
        job.setPostedByUserId(hrId);
        job = jobRepository.save(job);

        JobApplication application = new JobApplication();
        application.setJobId(job.getId());
        application.setCandidateId(candidateId);
        application.setStatus(ApplicationStatus.SHORTLISTED);
        applicationRepository.save(application);

        candidateToken = login("cand1");
        hrToken = login("hr1");
    }

    private Long saveUser(String username, Role role) {
        User user = new User();
        user.setFullName(username);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("Secret@123"));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user).getId();
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

    @Test
    void candidateDashboardReturnsOwnStatistics() throws Exception {
        mockMvc.perform(get("/api/candidates/dashboard")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(1))
                .andExpect(jsonPath("$.shortlistedApplications").value(1))
                .andExpect(jsonPath("$.offers").value(0))
                .andExpect(jsonPath("$.applicationsByStatus.length()").value(5))
                .andExpect(jsonPath("$.recentApplications.length()").value(1));
    }

    @Test
    void candidateDashboardRequiresCandidateRole() throws Exception {
        mockMvc.perform(get("/api/candidates/dashboard")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/candidates/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hrDashboardReturnsOwnStatistics() throws Exception {
        mockMvc.perform(get("/api/hr/dashboard")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(1))
                .andExpect(jsonPath("$.publishedJobs").value(1))
                .andExpect(jsonPath("$.totalApplicants").value(1))
                .andExpect(jsonPath("$.jobsByStatus.length()").value(3))
                .andExpect(jsonPath("$.applicationsByStatus.length()").value(5))
                .andExpect(jsonPath("$.applicantsPerJob.length()").value(1))
                .andExpect(jsonPath("$.recentApplications.length()").value(1));
    }

    @Test
    void hrDashboardRequiresHrRole() throws Exception {
        mockMvc.perform(get("/api/hr/dashboard")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/hr/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void emptyDashboardReturnsZerosAndEmptyLists() throws Exception {
        saveUser("fresh", Role.CANDIDATE);
        String freshToken = login("fresh");

        mockMvc.perform(get("/api/candidates/dashboard")
                        .header("Authorization", "Bearer " + freshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(0))
                .andExpect(jsonPath("$.applicationsByStatus.length()").value(5))
                .andExpect(jsonPath("$.recentApplications.length()").value(0));
    }
}
