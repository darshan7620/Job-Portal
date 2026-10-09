package com.jobportal.controller;

import com.jobportal.dto.ApplicationDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final JobApplicationService applicationService;

    public ApplicationController(JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDtos.ApplicationResponse apply(
            @PathVariable Long jobId,
            @Valid @RequestBody(required = false) ApplicationDtos.CreateApplicationRequest request,
            @AuthenticationPrincipal AppUserDetails principal) {
        ApplicationDtos.CreateApplicationRequest effectiveRequest =
                request != null ? request : new ApplicationDtos.CreateApplicationRequest();
        return applicationService.apply(jobId, effectiveRequest, principal.getId());
    }

    @GetMapping("/applications/{applicationId}")
    public ApplicationDtos.ApplicationResponse get(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUserDetails principal) {
        return applicationService.getApplication(applicationId, principal.getId(), principal.getRole());
    }

    @DeleteMapping("/applications/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable Long applicationId,
                         @AuthenticationPrincipal AppUserDetails principal) {
        applicationService.withdraw(applicationId, principal.getId());
    }
}
