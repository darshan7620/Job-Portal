package com.jobportal.service;

import com.jobportal.dto.JobDtos;
import com.jobportal.dto.PagedResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobStatus;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenOperationException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserService userService;

    public JobService(JobRepository jobRepository, UserService userService) {
        this.jobRepository = jobRepository;
        this.userService = userService;
    }

    @Transactional
    public JobDtos.JobResponse createJob(JobDtos.CreateJobRequest request, Long hrUserId) {
        userService.requireHr(hrUserId);
        validateSalary(request.getSalaryMin(), request.getSalaryMax());

        Job job = new Job();
        job.setTitle(request.getTitle().trim());
        job.setCompany(request.getCompany().trim());
        job.setLocation(trimToNull(request.getLocation()));
        job.setDescription(request.getDescription());
        job.setRequiredSkills(trimToNull(request.getRequiredSkills()));
        job.setEmploymentType(trimToNull(request.getEmploymentType()));
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setStatus(request.getStatus() != null ? request.getStatus() : JobStatus.DRAFT);
        job.setExpiresAt(request.getExpiresAt());
        job.setPostedByUserId(hrUserId);

        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobDtos.JobResponse updateJob(Long id, JobDtos.UpdateJobRequest request, Long hrUserId) {
        Job job = requireOwnedJob(id, hrUserId);

        if (isNotBlank(request.getTitle())) job.setTitle(request.getTitle().trim());
        if (isNotBlank(request.getCompany())) job.setCompany(request.getCompany().trim());
        if (request.getLocation() != null) job.setLocation(trimToNull(request.getLocation()));
        if (isNotBlank(request.getDescription())) job.setDescription(request.getDescription());
        if (request.getRequiredSkills() != null) job.setRequiredSkills(trimToNull(request.getRequiredSkills()));
        if (request.getEmploymentType() != null) job.setEmploymentType(trimToNull(request.getEmploymentType()));

        Integer salaryMin = request.getSalaryMin() != null ? request.getSalaryMin() : job.getSalaryMin();
        Integer salaryMax = request.getSalaryMax() != null ? request.getSalaryMax() : job.getSalaryMax();
        validateSalary(salaryMin, salaryMax);
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());

        if (request.getStatus() != null) job.setStatus(request.getStatus());
        if (request.getExpiresAt() != null) job.setExpiresAt(request.getExpiresAt());

        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id, Long hrUserId) {
        Job job = requireOwnedJob(id, hrUserId);
        jobRepository.delete(job);
    }

    @Transactional(readOnly = true)
    public JobDtos.JobResponse getJob(Long id) {
        return toResponse(getJobEntity(id));
    }

    @Transactional(readOnly = true)
    public List<JobDtos.JobResponse> searchPublished(String keyword, String location) {
        return jobRepository.searchPublished(blankToNull(keyword), blankToNull(location), null,
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobDtos.JobResponse> searchPublishedJobs(String keyword,
                                                                  String location,
                                                                  String employmentType,
                                                                  int page,
                                                                  int size,
                                                                  String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, resolveSort(sort));
        return PagedResponse.from(
                jobRepository.searchPublished(
                        blankToNull(keyword), blankToNull(location), blankToNull(employmentType), pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobDtos.JobResponse getPublishedJob(Long id) {
        Job job = getJobEntity(id);
        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Job not found");
        }
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobDtos.JobResponse> listByHr(Long hrUserId) {
        userService.requireHr(hrUserId);
        return jobRepository.findByPostedByUserIdOrderByCreatedAtDesc(hrUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobDtos.JobResponse getOwnedJob(Long id, Long hrUserId) {
        userService.requireHr(hrUserId);
        return toResponse(requireOwnedJob(id, hrUserId));
    }

    @Transactional(readOnly = true)
    public Job getJobEntity(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Transactional(readOnly = true)
    public Job requireOwnedJob(Long id, Long hrUserId) {
        Job job = getJobEntity(id);
        if (!job.getPostedByUserId().equals(hrUserId)) {
            throw new ForbiddenOperationException("You do not own this job posting");
        }
        return job;
    }

    private JobDtos.JobResponse toResponse(Job job) {
        JobDtos.JobResponse response = new JobDtos.JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setCompany(job.getCompany());
        response.setLocation(job.getLocation());
        response.setDescription(job.getDescription());
        response.setRequiredSkills(job.getRequiredSkills());
        response.setEmploymentType(job.getEmploymentType());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setStatus(job.getStatus());
        response.setExpiresAt(job.getExpiresAt());
        response.setPostedByUserId(job.getPostedByUserId());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }

    private static void validateSalary(Integer min, Integer max) {
        if (min != null && min < 0) {
            throw new BadRequestException("Minimum salary cannot be negative");
        }
        if (max != null && max < 0) {
            throw new BadRequestException("Maximum salary cannot be negative");
        }
        if (min != null && max != null && min > max) {
            throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
        }
    }

    private static Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort.trim().toLowerCase()) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "title", "title_asc" -> Sort.by(Sort.Direction.ASC, "title");
            case "title_desc" -> Sort.by(Sort.Direction.DESC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String blankToNull(String value) {
        return isNotBlank(value) ? value.trim() : null;
    }
}
