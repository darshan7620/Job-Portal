package com.jobportal.controller;

import com.jobportal.dto.JobDtos;
import com.jobportal.dto.PagedResponse;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // Public browse. Only PUBLISHED jobs are returned.
    @GetMapping
    public PagedResponse<JobDtos.JobResponse> browse(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return jobService.searchPublishedJobs(keyword, location, type, page, size, sort);
    }

    @GetMapping("/search")
    public PagedResponse<JobDtos.JobResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return jobService.searchPublishedJobs(keyword, location, type, page, size, sort);
    }

    @GetMapping("/{jobId}")
    public JobDtos.JobResponse get(@PathVariable Long jobId) {
        return jobService.getPublishedJob(jobId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobDtos.JobResponse create(@Valid @RequestBody JobDtos.CreateJobRequest request,
                                      @AuthenticationPrincipal AppUserDetails principal) {
        return jobService.createJob(request, principal.getId());
    }

    @PutMapping("/{jobId}")
    public JobDtos.JobResponse update(@PathVariable Long jobId,
                                      @Valid @RequestBody JobDtos.UpdateJobRequest request,
                                      @AuthenticationPrincipal AppUserDetails principal) {
        return jobService.updateJob(jobId, request, principal.getId());
    }

    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long jobId,
                       @AuthenticationPrincipal AppUserDetails principal) {
        jobService.deleteJob(jobId, principal.getId());
    }
}
