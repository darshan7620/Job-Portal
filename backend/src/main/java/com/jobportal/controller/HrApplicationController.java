package com.jobportal.controller;

import com.jobportal.dto.ApplicationDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
public class HrApplicationController {

    private final JobApplicationService applicationService;

    public HrApplicationController(JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/jobs/{jobId}/applications")
    public List<ApplicationDtos.ApplicationResponse> applicants(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AppUserDetails principal) {
        return applicationService.listForJob(jobId, principal.getId());
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ApplicationDtos.ApplicationResponse updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationDtos.UpdateApplicationStatusRequest request,
            @AuthenticationPrincipal AppUserDetails principal) {
        return applicationService.updateStatus(applicationId, request, principal.getId());
    }
}
