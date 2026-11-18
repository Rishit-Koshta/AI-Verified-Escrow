package com.escrow.backend.project.application;

import com.escrow.backend.project.application.dto.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(
            @PathVariable Long projectId,
            @Valid @RequestBody ApplyProjectRequest request,
            Authentication authentication) {

        return applicationService.apply(
                projectId, request, authentication.getName());
    }

    @GetMapping
    public List<ApplicationResponse> getApplications(
            @PathVariable Long projectId,
            Authentication authentication) {

        return applicationService.getProjectApplications(
                projectId, authentication.getName());
    }

    @PutMapping("/{applicationId}/accept")
    public Object acceptApplication(
            @PathVariable Long projectId,
            @PathVariable Long applicationId,
            Authentication authentication) {

        return applicationService.acceptApplication(
                projectId, applicationId, authentication.getName());
    }

    @PutMapping("/{applicationId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectApplication(
            @PathVariable Long projectId,
            @PathVariable Long applicationId,
            Authentication authentication) {

        applicationService.rejectApplication(
                projectId, applicationId, authentication.getName());
    }

    @DeleteMapping("/my-application")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawApplication(
            @PathVariable Long projectId,
            Authentication authentication) {

        applicationService.withdrawApplication(
                projectId, authentication.getName());
    }
}

