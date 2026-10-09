package com.jobportal.service;

import com.jobportal.dto.DashboardDtos;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    private Long hr1;
    private Long hr2;
    private Long emptyHr;
    private Long candidate1;
    private Long candidate2;
    private Long emptyCandidate;

    private Job job1;
    private Job job2;
    private Job job3;
    private Job otherHrJob;

    @BeforeEach
    void setUp() {
        hr1 = saveUser("hr1", Role.HR);
        hr2 = saveUser("hr2", Role.HR);
        emptyHr = saveUser("hr3", Role.HR);
        candidate1 = saveUser("cand1", Role.CANDIDATE);
        candidate2 = saveUser("cand2", Role.CANDIDATE);
        emptyCandidate = saveUser("cand3", Role.CANDIDATE);

        job1 = saveJob(hr1, "Backend Engineer", JobStatus.PUBLISHED);
        job2 = saveJob(hr1, "Frontend Engineer", JobStatus.PUBLISHED);
        job3 = saveJob(hr1, "QA Engineer", JobStatus.PUBLISHED);
        otherHrJob = saveJob(hr2, "Data Engineer", JobStatus.PUBLISHED);

        saveApplication(job1.getId(), candidate1, ApplicationStatus.SHORTLISTED);
        saveApplication(job2.getId(), candidate1, ApplicationStatus.HIRED);
        saveApplication(job3.getId(), candidate1, ApplicationStatus.REJECTED);
        saveApplication(job1.getId(), candidate2, ApplicationStatus.APPLIED);
        saveApplication(otherHrJob.getId(), candidate2, ApplicationStatus.UNDER_REVIEW);
    }

    private Long saveUser(String username, Role role) {
        User user = new User();
        user.setFullName(username);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz012345678901234567890123456");
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user).getId();
    }

    private Job saveJob(Long ownerId, String title, JobStatus status) {
        Job job = new Job();
        job.setTitle(title);
        job.setCompany("Acme");
        job.setDescription("Description");
        job.setStatus(status);
        job.setPostedByUserId(ownerId);
        return jobRepository.save(job);
    }

    private void saveApplication(Long jobId, Long candidateId, ApplicationStatus status) {
        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setCandidateId(candidateId);
        application.setStatus(status);
        applicationRepository.save(application);
    }

    private Map<ApplicationStatus, Long> applicationBreakdown(DashboardDtos.CandidateDashboardResponse response) {
        return response.getApplicationsByStatus().stream()
                .collect(Collectors.toMap(DashboardDtos.ApplicationStatusCount::getStatus,
                        DashboardDtos.ApplicationStatusCount::getCount));
    }

    private Map<ApplicationStatus, Long> applicationBreakdown(DashboardDtos.HrDashboardResponse response) {
        return response.getApplicationsByStatus().stream()
                .collect(Collectors.toMap(DashboardDtos.ApplicationStatusCount::getStatus,
                        DashboardDtos.ApplicationStatusCount::getCount));
    }

    private Map<JobStatus, Long> jobBreakdown(DashboardDtos.HrDashboardResponse response) {
        return response.getJobsByStatus().stream()
                .collect(Collectors.toMap(DashboardDtos.JobStatusCount::getStatus,
                        DashboardDtos.JobStatusCount::getCount));
    }

    @Test
    void candidateDashboardAggregatesOwnApplications() {
        DashboardDtos.CandidateDashboardResponse response = dashboardService.getCandidateDashboard(candidate1);

        assertThat(response.getTotalApplications()).isEqualTo(3);
        assertThat(response.getShortlistedApplications()).isEqualTo(1);
        assertThat(response.getRejectedApplications()).isEqualTo(1);
        assertThat(response.getOffers()).isEqualTo(1);
        assertThat(response.getUnderReviewApplications()).isZero();
        assertThat(applicationBreakdown(response))
                .containsEntry(ApplicationStatus.APPLIED, 0L)
                .containsEntry(ApplicationStatus.UNDER_REVIEW, 0L)
                .containsEntry(ApplicationStatus.SHORTLISTED, 1L)
                .containsEntry(ApplicationStatus.REJECTED, 1L)
                .containsEntry(ApplicationStatus.HIRED, 1L);
        assertThat(response.getApplicationsByStatus()).hasSize(ApplicationStatus.values().length);
        assertThat(response.getRecentApplications()).hasSize(3);
        assertThat(response.getRecentlyUpdatedApplications()).hasSize(3);
    }

    @Test
    void candidateDashboardIsIsolatedPerCandidate() {
        DashboardDtos.CandidateDashboardResponse response = dashboardService.getCandidateDashboard(candidate2);

        assertThat(response.getTotalApplications()).isEqualTo(2);
        assertThat(response.getOffers()).isZero();
        assertThat(applicationBreakdown(response))
                .containsEntry(ApplicationStatus.APPLIED, 1L)
                .containsEntry(ApplicationStatus.UNDER_REVIEW, 1L);
    }

    @Test
    void candidateDashboardIsEmptyWhenNoApplications() {
        DashboardDtos.CandidateDashboardResponse response = dashboardService.getCandidateDashboard(emptyCandidate);

        assertThat(response.getTotalApplications()).isZero();
        assertThat(response.getShortlistedApplications()).isZero();
        assertThat(response.getRejectedApplications()).isZero();
        assertThat(response.getOffers()).isZero();
        assertThat(response.getApplicationsByStatus())
                .allSatisfy(entry -> assertThat(entry.getCount()).isZero());
        assertThat(response.getRecentApplications()).isEmpty();
        assertThat(response.getRecentlyUpdatedApplications()).isEmpty();
    }

    @Test
    void hrDashboardCountsOwnJobsAndApplicants() {
        DashboardDtos.HrDashboardResponse response = dashboardService.getHrDashboard(hr1);

        assertThat(response.getTotalJobs()).isEqualTo(3);
        assertThat(response.getPublishedJobs()).isEqualTo(3);
        assertThat(response.getDraftJobs()).isZero();
        assertThat(response.getClosedJobs()).isZero();
        assertThat(response.getTotalApplicants()).isEqualTo(4);
        assertThat(jobBreakdown(response))
                .containsEntry(JobStatus.DRAFT, 0L)
                .containsEntry(JobStatus.PUBLISHED, 3L)
                .containsEntry(JobStatus.CLOSED, 0L);
        assertThat(applicationBreakdown(response))
                .containsEntry(ApplicationStatus.APPLIED, 1L)
                .containsEntry(ApplicationStatus.SHORTLISTED, 1L)
                .containsEntry(ApplicationStatus.REJECTED, 1L)
                .containsEntry(ApplicationStatus.HIRED, 1L);
        assertThat(response.getRecentApplications()).hasSize(4);
        assertThat(response.getApplicantsPerJob()).hasSize(3);
        assertThat(response.getTopJobsByApplicants()).isNotEmpty();
        assertThat(response.getTopJobsByApplicants().get(0).getJobId()).isEqualTo(job1.getId());
        assertThat(response.getTopJobsByApplicants().get(0).getApplicantCount()).isEqualTo(2);
    }

    @Test
    void hrDashboardIsIsolatedPerHrUser() {
        DashboardDtos.HrDashboardResponse response = dashboardService.getHrDashboard(hr2);

        assertThat(response.getTotalJobs()).isEqualTo(1);
        assertThat(response.getTotalApplicants()).isEqualTo(1);
        assertThat(response.getApplicantsPerJob()).hasSize(1);
        assertThat(response.getApplicantsPerJob().get(0).getJobId()).isEqualTo(otherHrJob.getId());
    }

    @Test
    void hrDashboardIsEmptyWhenNoJobs() {
        DashboardDtos.HrDashboardResponse response = dashboardService.getHrDashboard(emptyHr);

        assertThat(response.getTotalJobs()).isZero();
        assertThat(response.getTotalApplicants()).isZero();
        assertThat(response.getJobsByStatus())
                .allSatisfy(entry -> assertThat(entry.getCount()).isZero());
        assertThat(response.getApplicationsByStatus())
                .allSatisfy(entry -> assertThat(entry.getCount()).isZero());
        assertThat(response.getRecentApplications()).isEmpty();
        assertThat(response.getApplicantsPerJob()).isEmpty();
        assertThat(response.getTopJobsByApplicants()).isEmpty();
    }
}
