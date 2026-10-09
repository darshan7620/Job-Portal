package com.jobportal.service;

import com.jobportal.dto.ApplicationDtos;
import com.jobportal.dto.JobDtos;
import com.jobportal.dto.UserDtos;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.JobStatus;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenOperationException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JobApplicationServiceTest {

    @Autowired
    private JobApplicationService applicationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    private Long hrId;
    private Long otherHrId;
    private Long candidateId;
    private Long publishedJobId;
    private Long draftJobId;

    @BeforeEach
    void setUp() {
        hrId = userRepository.save(user("hr1", "hr1@example.com", Role.HR)).getId();
        otherHrId = userRepository.save(user("hr2", "hr2@example.com", Role.HR)).getId();
        candidateId = userService.registerCandidate(candidateRequest("jane", "jane@example.com")).getId();
        publishedJobId = jobService.createJob(jobRequest("Live Role", JobStatus.PUBLISHED), hrId).getId();
        draftJobId = jobService.createJob(jobRequest("Draft Role", JobStatus.DRAFT), hrId).getId();
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

    private UserDtos.RegisterCandidateRequest candidateRequest(String username, String email) {
        UserDtos.RegisterCandidateRequest request = new UserDtos.RegisterCandidateRequest();
        request.setFullName("Jane Candidate");
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("Secret@123");
        return request;
    }

    private JobDtos.CreateJobRequest jobRequest(String title, JobStatus status) {
        JobDtos.CreateJobRequest request = new JobDtos.CreateJobRequest();
        request.setTitle(title);
        request.setCompany("TechNova");
        request.setDescription("Description");
        request.setStatus(status);
        return request;
    }

    private ApplicationDtos.CreateApplicationRequest coverLetter() {
        return new ApplicationDtos.CreateApplicationRequest();
    }

    @Test
    void applyCreatesApplicationWithAppliedStatus() {
        var response = applicationService.apply(publishedJobId, coverLetter(), candidateId);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.getJobId()).isEqualTo(publishedJobId);
        assertThat(response.getCandidateId()).isEqualTo(candidateId);
        assertThat(response.getAppliedAt()).isNotNull();
    }

    @Test
    void applyRejectsDuplicate() {
        applicationService.apply(publishedJobId, coverLetter(), candidateId);

        assertThatThrownBy(() -> applicationService.apply(publishedJobId, coverLetter(), candidateId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already applied");
    }

    @Test
    void applyRejectsUnpublishedJob() {
        assertThatThrownBy(() -> applicationService.apply(draftJobId, coverLetter(), candidateId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not open");
    }

    @Test
    void updateStatusByOwnerHrSucceeds() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();
        ApplicationDtos.UpdateApplicationStatusRequest request = new ApplicationDtos.UpdateApplicationStatusRequest();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        assertThat(applicationService.updateStatus(applicationId, request, hrId).getStatus())
                .isEqualTo(ApplicationStatus.SHORTLISTED);
    }

    @Test
    void updateStatusByOtherHrIsRejected() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();
        ApplicationDtos.UpdateApplicationStatusRequest request = new ApplicationDtos.UpdateApplicationStatusRequest();
        request.setStatus(ApplicationStatus.REJECTED);

        assertThatThrownBy(() -> applicationService.updateStatus(applicationId, request, otherHrId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void withdrawRemovesApplication() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();

        applicationService.withdraw(applicationId, candidateId);

        assertThat(applicationRepository.findById(applicationId)).isEmpty();
    }

    private ApplicationDtos.UpdateApplicationStatusRequest status(ApplicationStatus status) {
        ApplicationDtos.UpdateApplicationStatusRequest request =
                new ApplicationDtos.UpdateApplicationStatusRequest();
        request.setStatus(status);
        return request;
    }

    @Test
    void updateStatusRejectsInvalidTransition() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();

        assertThatThrownBy(() -> applicationService.updateStatus(
                applicationId, status(ApplicationStatus.HIRED), hrId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatusAllowsShortlistedToHired() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();
        applicationService.updateStatus(applicationId, status(ApplicationStatus.SHORTLISTED), hrId);

        ApplicationDtos.ApplicationResponse response =
                applicationService.updateStatus(applicationId, status(ApplicationStatus.HIRED), hrId);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.HIRED);
    }

    @Test
    void updateStatusRejectsTransitionOutOfTerminalState() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();
        applicationService.updateStatus(applicationId, status(ApplicationStatus.REJECTED), hrId);

        assertThatThrownBy(() -> applicationService.updateStatus(
                applicationId, status(ApplicationStatus.UNDER_REVIEW), hrId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void withdrawRejectsTerminalApplication() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();
        applicationService.updateStatus(applicationId, status(ApplicationStatus.SHORTLISTED), hrId);
        applicationService.updateStatus(applicationId, status(ApplicationStatus.HIRED), hrId);

        assertThatThrownBy(() -> applicationService.withdraw(applicationId, candidateId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer be withdrawn");
    }

    @Test
    void getApplicationAllowsOwnerAndOwningHr() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();

        assertThat(applicationService.getApplication(applicationId, candidateId, Role.CANDIDATE).getId())
                .isEqualTo(applicationId);
        assertThat(applicationService.getApplication(applicationId, hrId, Role.HR).getId())
                .isEqualTo(applicationId);
    }

    @Test
    void getApplicationRejectsOtherCandidateAndHr() {
        Long applicationId = applicationService.apply(publishedJobId, coverLetter(), candidateId).getId();
        Long otherCandidateId = userRepository.save(user("cand2", "cand2@example.com", Role.CANDIDATE)).getId();

        assertThatThrownBy(() -> applicationService.getApplication(
                applicationId, otherCandidateId, Role.CANDIDATE))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> applicationService.getApplication(applicationId, otherHrId, Role.HR))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
