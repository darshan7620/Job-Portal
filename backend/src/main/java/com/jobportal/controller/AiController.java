package com.jobportal.controller;

import com.jobportal.ai.AiMatcherService;
import com.jobportal.dto.AiDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiMatcherService aiMatcherService;

    public AiController(AiMatcherService aiMatcherService) {
        this.aiMatcherService = aiMatcherService;
    }

    @PostMapping("/match")
    public AiDtos.MatchResponse match(@Valid @RequestBody AiDtos.MatchRequest request) {
        return aiMatcherService.match(request);
    }
}
