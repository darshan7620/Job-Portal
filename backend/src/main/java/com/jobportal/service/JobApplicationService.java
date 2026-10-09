package com.jobportal.service;

import com.jobportal.dto.ApplicationDtos;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.JobStatus;
import com.jobportal.entity.Role;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenOperationException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobApplicationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JobApplicationService {

    private static final Set<ApplicationStatus> TERMINAL_STATUSES =
            EnumSet.of(ApplicationStatus.REJECTED, ApplicationStatus.HIRED);

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ApplicationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ApplicationStatus.APPLIED,
                EnumSet.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED,
                        ApplicationStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.UNDER_REVIEW,
                EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED,
                        ApplicationStatus.HIRED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.SHORTLISTED,
                EnumSet.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.REJECTED,
                        ApplicationStatus.HIRED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.HIRED, EnumSet.noneOf(ApplicationStatus.class));
    }

    private final JobApplicationRepository applicationRepository;
    private final JobService jobService;
    private final UserService userService;

    public JobApplicationService(JobApplicationRepository applicationRepository,
                                 JobService jobService,
                                 UserService userService) {
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.userService = userService;
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse apply(Long jobId,
                                                     ApplicationDtos.CreateApplicationRequest request,
                                                     Long candidateId) {
        userService.requireCandidate(candidateId);
        Job job = jobService.getJobEntity(jobId);

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("This job is not open for applications");
        }
        if (job.getExpiresAt() != null && job.getExpiresAt().isBefore(LocalDate.now())) {
            throw new BadRequestException("This job posting has expired");
        }
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new BadRequestException("You have already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setCandidateId(candidateId);
        application.setCoverLetter(trimToNull(request.getCoverLetter()));
        application.setStatus(ApplicationStatus.APPLIED);

        try {
            return toResponse(applicationRepository.saveAndFlush(application));
        } catch (DataIntegrityViolationException ex) {
            // The database unique constraint (uq_job_candidate) is the last line of
            // defence against two concurrent applications slipping past the check above.
            throw new BadRequestException("You have already applied for this job");
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationDtos.ApplicationResponse> listForJob(Long jobId, Long hrUserId) {
        jobService.requireOwnedJob(jobId, hrUserId);
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationDtos.ApplicationResponse> listForCandidate(Long candidateId) {
        userService.requireCandidate(candidateId);
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidateId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse updateStatus(Long applicationId,
                                                            ApplicationDtos.UpdateApplicationStatusRequest request,
                                                            Long hrUserId) {
        userService.requireHr(hrUserId);
        JobApplication application = getApplicationEntity(applicationId);
        jobService.requireOwnedJob(application.getJobId(), hrUserId);

        ApplicationStatus current = application.getStatus();
        ApplicationStatus target = request.getStatus();
        if (current != target && !ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BadRequestException(
                    "Invalid status transition from " + current + " to " + target);
        }
        application.setStatus(target);
        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public void withdraw(Long applicationId, Long candidateId) {
        userService.requireCandidate(candidateId);
        JobApplication application = applicationRepository.findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (TERMINAL_STATUSES.contains(application.getStatus())) {
            throw new BadRequestException(
                    "This application can no longer be withdrawn because it is already "
                            + application.getStatus().name().toLowerCase().replace('_', ' '));
        }
        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public ApplicationDtos.ApplicationResponse getApplication(Long applicationId, Long userId, Role role) {
        JobApplication application = getApplicationEntity(applicationId);
        if (role == Role.HR) {
            jobService.requireOwnedJob(application.getJobId(), userId);
        } else if (role == Role.CANDIDATE) {
            if (!application.getCandidateId().equals(userId)) {
                throw new ForbiddenOperationException("You do not have access to this application");
            }
        } else {
            throw new ForbiddenOperationException("You do not have access to this application");
        }
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public JobApplication getApplicationEntity(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private ApplicationDtos.ApplicationResponse toResponse(JobApplication application) {
        ApplicationDtos.ApplicationResponse response = new ApplicationDtos.ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJobId());
        response.setJobTitle(jobService.getJobEntity(application.getJobId()).getTitle());
        response.setCandidateId(application.getCandidateId());
        response.setCandidateName(userService.requireUser(application.getCandidateId()).getFullName());
        response.setCoverLetter(application.getCoverLetter());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
