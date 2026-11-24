package com.escrow.backend.project.application;


import com.escrow.backend.project.*;
import com.escrow.backend.project.auth.*;
import com.escrow.backend.project.application.dto.*;
import com.escrow.backend.project.dto.ProjectResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ApplicationService {

    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final AuthRepository authRepository;

    public ApplicationService(
            ProjectRepository projectRepository,
            ApplicationRepository applicationRepository,
            AuthRepository authRepository) {

        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
        this.authRepository = authRepository;
    }

    private AppUser getUser(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void requireFreelancer(AppUser user) {
        if (user.getRole() != UserRole.FREELANCER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only freelancers can apply");
        }
    }

    private void requireClientOwner(Project project, AppUser user) {
        if (user.getRole() != UserRole.CLIENT ||
                !project.getClient().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the project owner can manage applications");
        }
    }

    @Transactional
    public ApplicationResponse apply(
            Long projectId,
            ApplyProjectRequest request,
            String email) {

        AppUser freelancer = getUser(email);
        requireFreelancer(freelancer);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This project is not accepting applications");
        }

        if (project.getClient().getId().equals(freelancer.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot apply to your own project");
        }

        if (applicationRepository.existsByProjectIdAndFreelancerId(
                projectId, freelancer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already applied to this project");
        }

        ProjectApplication application = new ProjectApplication(
                project,
                freelancer,
                request.proposal().trim(),
                request.proposedBudget(),
                request.estimatedDays()
        );

        return ApplicationResponse.from(
                applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getProjectApplications(
            Long projectId,
            String email) {

        AppUser user = getUser(email);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));

        requireClientOwner(project, user);

        return applicationRepository
                .findByProjectIdOrderByAppliedAtDesc(projectId)
                .stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String email) {
        AppUser user = getUser(email);
        requireFreelancer(user);

        return applicationRepository
                .findByFreelancerIdOrderByAppliedAtDesc(user.getId())
                .stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional
    public ProjectResponse acceptApplication(
            Long projectId,
            Long applicationId,
            String email) {

        AppUser client = getUser(email);

        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));

        requireClientOwner(project, client);

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Project has already been assigned or closed");
        }

        ProjectApplication selected =
                applicationRepository
                        .findByIdAndProjectId(applicationId, projectId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"));

        if (selected.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This application is no longer pending");
        }

        project.assignFreelancer(selected.getFreelancer());
        selected.accept();
        project = projectRepository.save(project);
        selected = applicationRepository.save(selected);

        List<ProjectApplication> applications =
                applicationRepository
                        .findByProjectIdOrderByAppliedAtDesc(projectId);

        for (ProjectApplication application : applications) {
            if (!application.getId().equals(selected.getId()) &&
                    application.getStatus() == ApplicationStatus.PENDING) {
                application.reject();
                applicationRepository.save(application);
            }
        }

        return ProjectResponse.from(project);
    }

    @Transactional
    public void rejectApplication(
            Long projectId,
            Long applicationId,
            String email) {

        AppUser client = getUser(email);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));

        requireClientOwner(project, client);

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Applications can only be rejected for open projects");
        }

        ProjectApplication application =
                applicationRepository
                        .findByIdAndProjectId(applicationId, projectId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Application is not pending");
        }

        application.reject();
    }

    @Transactional
    public void withdrawApplication(
            Long projectId,
            String email) {

        AppUser freelancer = getUser(email);
        requireFreelancer(freelancer);

        ProjectApplication application =
                applicationRepository
                        .findByProjectIdAndFreelancerId(
                                projectId, freelancer.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending applications can be withdrawn");
        }

        application.withdraw();
    }
}
