package com.jobportal.controller;

import com.jobportal.dto.DashboardDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates/dashboard")
public class CandidateDashboardController {

    private final DashboardService dashboardService;

    public CandidateDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDtos.CandidateDashboardResponse dashboard(
            @AuthenticationPrincipal AppUserDetails principal) {
        return dashboardService.getCandidateDashboard(principal.getId());
    }
}
