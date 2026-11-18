package com.escrow.backend.project;

import com.escrow.backend.project.dto.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {

        return projectService.createProject(
                request, authentication.getName());
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/available")
    public List<ProjectResponse> getAvailableProjects() {
        return projectService.getAvailableProjects();
    }

    @GetMapping("/my-projects")
    public List<ProjectResponse> getMyProjects(
            Authentication authentication) {

        return projectService.getMyProjects(authentication.getName());
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(
            @PathVariable Long id) {

        return projectService.getProjectById(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {

        return projectService.updateProject(
                id, request, authentication.getName());
    }

    @PutMapping("/{id}/status")
    public ProjectResponse updateProjectStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectStatusRequest request,
            Authentication authentication) {

        return projectService.updateProjectStatus(
                id, request.status(), authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @PathVariable Long id,
            Authentication authentication) {

        projectService.deleteProject(id, authentication.getName());
    }
}
