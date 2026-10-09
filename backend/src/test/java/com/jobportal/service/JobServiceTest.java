package com.jobportal.service;

import com.jobportal.dto.JobDtos;
import com.jobportal.entity.JobStatus;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenOperationException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JobServiceTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    private Long hrId;
    private Long secondHrId;

    @BeforeEach
    void setUp() {
        hrId = userRepository.save(user("hr1", "hr1@example.com", Role.HR)).getId();
        secondHrId = userRepository.save(user("hr2", "hr2@example.com", Role.HR)).getId();
    }

    private User user(String username, String email, Role role) {
        User user = new User();
        user.setFullName(username);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz012345678901234567890123456");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private JobDtos.CreateJobRequest createRequest(String title, JobStatus status) {
        JobDtos.CreateJobRequest request = new JobDtos.CreateJobRequest();
        request.setTitle(title);
        request.setCompany("TechNova");
        request.setDescription("Some description");
        request.setStatus(status);
        return request;
    }

    @Test
    void createJobDefaultsToDraftWhenStatusMissing() {
        JobDtos.JobResponse response = jobService.createJob(createRequest("Backend Developer", null), hrId);

        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(response.getPostedByUserId()).isEqualTo(hrId);
    }

    @Test
    void createJobRejectsCandidate() {
        Long candidateId = userRepository.save(user("cand", "cand@example.com", Role.CANDIDATE)).getId();

        assertThatThrownBy(() -> jobService.createJob(createRequest("Job", JobStatus.PUBLISHED), candidateId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void searchPublishedReturnsOnlyPublishedJobs() {
        jobService.createJob(createRequest("Draft Role", JobStatus.DRAFT), hrId);
        jobService.createJob(createRequest("Live Role", JobStatus.PUBLISHED), hrId);

        List<JobDtos.JobResponse> results = jobService.searchPublished("Role", null);

        assertThat(results).extracting(JobDtos.JobResponse::getTitle).containsExactly("Live Role");
    }

    @Test
    void updateJobRejectsNonOwner() {
        Long jobId = jobService.createJob(createRequest("Owner Role", JobStatus.PUBLISHED), hrId).getId();

        JobDtos.UpdateJobRequest update = new JobDtos.UpdateJobRequest();
        update.setTitle("Hijacked");

        assertThatThrownBy(() -> jobService.updateJob(jobId, update, secondHrId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deleteJobRemovesOwnedJob() {
        Long jobId = jobService.createJob(createRequest("Temp Role", JobStatus.DRAFT), hrId).getId();

        jobService.deleteJob(jobId, hrId);

        assertThat(jobRepository.findById(jobId)).isEmpty();
    }

    @Test
    void createJobRejectsSalaryMinGreaterThanMax() {
        JobDtos.CreateJobRequest request = createRequest("Salary Role", JobStatus.DRAFT);
        request.setSalaryMin(90000);
        request.setSalaryMax(50000);

        assertThatThrownBy(() -> jobService.createJob(request, hrId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum salary");
    }

    @Test
    void createJobRejectsNegativeSalary() {
        JobDtos.CreateJobRequest request = createRequest("Negative Role", JobStatus.DRAFT);
        request.setSalaryMin(-1);

        assertThatThrownBy(() -> jobService.createJob(request, hrId))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateJobCanPublishAndClose() {
        Long jobId = jobService.createJob(createRequest("Lifecycle Role", JobStatus.DRAFT), hrId).getId();

        JobDtos.UpdateJobRequest publish = new JobDtos.UpdateJobRequest();
        publish.setStatus(JobStatus.PUBLISHED);
        assertThat(jobService.updateJob(jobId, publish, hrId).getStatus()).isEqualTo(JobStatus.PUBLISHED);

        JobDtos.UpdateJobRequest close = new JobDtos.UpdateJobRequest();
        close.setStatus(JobStatus.CLOSED);
        assertThat(jobService.updateJob(jobId, close, hrId).getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void getPublishedJobHidesDraft() {
        Long draftId = jobService.createJob(createRequest("Hidden Role", JobStatus.DRAFT), hrId).getId();

        assertThatThrownBy(() -> jobService.getPublishedJob(draftId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchJobsFiltersByTypeAndKeyword() {
        JobDtos.CreateJobRequest full = createRequest("Java Developer", JobStatus.PUBLISHED);
        full.setEmploymentType("FULL_TIME");
        jobService.createJob(full, hrId);

        JobDtos.CreateJobRequest intern = createRequest("Java Intern", JobStatus.PUBLISHED);
        intern.setEmploymentType("INTERNSHIP");
        jobService.createJob(intern, hrId);

        var page = jobService.searchPublishedJobs("Java", null, "INTERNSHIP", 0, 10, "newest");

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(JobDtos.JobResponse::getTitle)
                .containsExactly("Java Intern");
    }
}
