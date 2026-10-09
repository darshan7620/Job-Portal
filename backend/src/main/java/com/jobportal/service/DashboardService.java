package com.jobportal.service;

import com.jobportal.dto.ApplicationDtos;
import com.jobportal.dto.DashboardDtos;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.JobStatus;
import com.jobportal.entity.User;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int RECENT_LIMIT = 5;
    private static final int TOP_JOBS_LIMIT = 5;

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public DashboardService(JobRepository jobRepository,
                            JobApplicationRepository applicationRepository,
                            UserRepository userRepository,
                            UserService userService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.CandidateDashboardResponse getCandidateDashboard(Long candidateId) {
        userService.requireCandidate(candidateId);

        Map<ApplicationStatus, Long> counts = applicationRepository
                .countApplicationsByStatusForCandidate(candidateId)
                .stream()
                .collect(Collectors.toMap(
                        JobApplicationRepository.ApplicationStatusCount::getStatus,
                        JobApplicationRepository.ApplicationStatusCount::getTotal));

        List<JobApplication> recent =
                applicationRepository.findTop5ByCandidateIdOrderByAppliedAtDescIdDesc(candidateId);
        List<JobApplication> recentlyUpdated =
                applicationRepository.findTop5ByCandidateIdOrderByUpdatedAtDescIdDesc(candidateId);

        DashboardDtos.CandidateDashboardResponse response = new DashboardDtos.CandidateDashboardResponse();
        response.setTotalApplications(counts.values().stream().mapToLong(Long::longValue).sum());
        response.setUnderReviewApplications(countOf(counts, ApplicationStatus.UNDER_REVIEW));
        response.setShortlistedApplications(countOf(counts, ApplicationStatus.SHORTLISTED));
        response.setRejectedApplications(countOf(counts, ApplicationStatus.REJECTED));
        response.setOffers(countOf(counts, ApplicationStatus.HIRED));
        response.setApplicationsByStatus(toApplicationStatusCounts(counts));
        response.setRecentApplications(toApplicationResponses(recent));
        response.setRecentlyUpdatedApplications(toApplicationResponses(recentlyUpdated));
        return response;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.HrDashboardResponse getHrDashboard(Long hrUserId) {
        userService.requireHr(hrUserId);

        Map<JobStatus, Long> jobCounts = jobRepository.countJobsByStatusForOwner(hrUserId)
                .stream()
                .collect(Collectors.toMap(
                        JobRepository.JobStatusCount::getStatus,
                        JobRepository.JobStatusCount::getTotal));

        Map<ApplicationStatus, Long> applicationCounts = applicationRepository
                .countApplicationsByStatusForOwner(hrUserId)
                .stream()
                .collect(Collectors.toMap(
                        JobApplicationRepository.ApplicationStatusCount::getStatus,
                        JobApplicationRepository.ApplicationStatusCount::getTotal));

        List<DashboardDtos.JobApplicantCount> applicantCounts = applicationRepository
                .findApplicantCountsByOwner(hrUserId)
                .stream()
                .map(this::toJobApplicantCount)
                .toList();

        List<JobApplication> recent = applicationRepository.findRecentByOwner(
                hrUserId, PageRequest.of(0, RECENT_LIMIT));

        DashboardDtos.HrDashboardResponse response = new DashboardDtos.HrDashboardResponse();
        response.setTotalJobs(jobRepository.countByPostedByUserId(hrUserId));
        response.setDraftJobs(countOf(jobCounts, JobStatus.DRAFT));
        response.setPublishedJobs(countOf(jobCounts, JobStatus.PUBLISHED));
        response.setClosedJobs(countOf(jobCounts, JobStatus.CLOSED));
        response.setTotalApplicants(applicationCounts.values().stream().mapToLong(Long::longValue).sum());
        response.setJobsByStatus(toJobStatusCounts(jobCounts));
        response.setApplicationsByStatus(toApplicationStatusCounts(applicationCounts));
        response.setRecentApplications(toApplicationResponses(recent));
        response.setApplicantsPerJob(applicantCounts);
        response.setTopJobsByApplicants(applicantCounts.stream().limit(TOP_JOBS_LIMIT).toList());
        return response;
    }

    private DashboardDtos.JobApplicantCount toJobApplicantCount(
            JobApplicationRepository.JobApplicantCount projection) {
        DashboardDtos.JobApplicantCount dto = new DashboardDtos.JobApplicantCount();
        dto.setJobId(projection.getJobId());
        dto.setJobTitle(projection.getJobTitle());
        dto.setStatus(projection.getStatus());
        dto.setApplicantCount(projection.getTotal());
        return dto;
    }

    private List<DashboardDtos.ApplicationStatusCount> toApplicationStatusCounts(
            Map<ApplicationStatus, Long> counts) {
        List<DashboardDtos.ApplicationStatusCount> result = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            result.add(new DashboardDtos.ApplicationStatusCount(status, countOf(counts, status)));
        }
        return result;
    }

    private List<DashboardDtos.JobStatusCount> toJobStatusCounts(Map<JobStatus, Long> counts) {
        List<DashboardDtos.JobStatusCount> result = new ArrayList<>();
        for (JobStatus status : JobStatus.values()) {
            result.add(new DashboardDtos.JobStatusCount(status, countOf(counts, status)));
        }
        return result;
    }

    // Resolves job titles and candidate names in two batch queries rather than
    // one lookup per application, keeping the recent lists cheap.
    private List<ApplicationDtos.ApplicationResponse> toApplicationResponses(
            List<JobApplication> applications) {
        if (applications.isEmpty()) {
            return List.of();
        }

        Set<Long> jobIds = applications.stream()
                .map(JobApplication::getJobId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> candidateIds = applications.stream()
                .map(JobApplication::getCandidateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> jobTitles = jobRepository.findAllById(jobIds).stream()
                .collect(Collectors.toMap(Job::getId, Job::getTitle, (a, b) -> a));
        Map<Long, String> candidateNames = userRepository.findAllById(candidateIds).stream()
                .collect(Collectors.toMap(User::getId, User::getFullName, (a, b) -> a));

        return applications.stream().map(application -> {
            ApplicationDtos.ApplicationResponse response = new ApplicationDtos.ApplicationResponse();
            response.setId(application.getId());
            response.setJobId(application.getJobId());
            response.setJobTitle(jobTitles.get(application.getJobId()));
            response.setCandidateId(application.getCandidateId());
            response.setCandidateName(candidateNames.get(application.getCandidateId()));
            response.setCoverLetter(application.getCoverLetter());
            response.setStatus(application.getStatus());
            response.setAppliedAt(application.getAppliedAt());
            response.setUpdatedAt(application.getUpdatedAt());
            return response;
        }).toList();
    }

    private static <E extends Enum<E>> long countOf(Map<E, Long> counts, E key) {
        return counts.getOrDefault(key, 0L);
    }
}
