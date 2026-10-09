package com.jobportal.controller;

import com.jobportal.dto.ApplicationDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.JobApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidates/applications")
public class CandidateApplicationController {

    private final JobApplicationService applicationService;

    public CandidateApplicationController(JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationDtos.ApplicationResponse> list(
            @AuthenticationPrincipal AppUserDetails principal) {
        return applicationService.listForCandidate(principal.getId());
    }
}
