package com.jobportal.controller;

import com.jobportal.dto.JobDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.JobService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hr/jobs")
public class HrJobController {

    private final JobService jobService;

    public HrJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobDtos.JobResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
        return jobService.listByHr(principal.getId());
    }

    @GetMapping("/{jobId}")
    public JobDtos.JobResponse get(@PathVariable Long jobId,
                                   @AuthenticationPrincipal AppUserDetails principal) {
        return jobService.getOwnedJob(jobId, principal.getId());
    }
}
