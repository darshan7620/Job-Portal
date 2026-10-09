package com.jobportal.controller;

import com.jobportal.dto.DashboardDtos;
import com.jobportal.security.AppUserDetails;
import com.jobportal.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/dashboard")
public class HrDashboardController {

    private final DashboardService dashboardService;

    public HrDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDtos.HrDashboardResponse dashboard(
            @AuthenticationPrincipal AppUserDetails principal) {
        return dashboardService.getHrDashboard(principal.getId());
    }
}
