package com.escrow.backend.project.application;

import com.escrow.backend.project.application.dto.ApplicationResponse;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class MyApplicationController {

    private final ApplicationService applicationService;

    public MyApplicationController(
            ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/my-applications")
    public List<ApplicationResponse> getMyApplications(
            Authentication authentication) {

        return applicationService.getMyApplications(
                authentication.getName());
    }
}
