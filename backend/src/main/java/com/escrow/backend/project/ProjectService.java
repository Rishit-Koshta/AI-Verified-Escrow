package com.escrow.backend.project;


import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.auth.AuthRepository;
import com.escrow.backend.project.auth.UserRole;
import com.escrow.backend.project.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AuthRepository authRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            AuthRepository authRepository) {
        this.projectRepository = projectRepository;
        this.authRepository = authRepository;
    }

    private AppUser getCurrentUser(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private void requireRole(AppUser user, UserRole role) {
        if (user.getRole() != role) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action");
        }
    }

    private void requireOwner(Project project, AppUser user) {
        if (!project.getClient().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the project owner can perform this action");
        }
    }

    @Transactional
    public void markProjectInProgress(Long projectId) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
            return;
        }

        if (project.getStatus() != ProjectStatus.ASSIGNED && project.getStatus() != ProjectStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only assigned projects can move to IN_PROGRESS"
            );
        }

        project.updateStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);
    }

    @Transactional
    public void markProjectCompleted(Long projectId) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.COMPLETED) {
            return;
        }

        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only in-progress projects can be completed"
            );
        }

        project.updateStatus(ProjectStatus.COMPLETED);
        projectRepository.save(project);
    }

    @Transactional
    public void markProjectCancelled(Long projectId) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.CANCELLED) {
            return;
        }

        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only in-progress projects can be cancelled through escrow"
            );
        }

        project.updateStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request,
            String email) {

        AppUser client = getCurrentUser(email);
        requireRole(client, UserRole.CLIENT);

        Project project = new Project(
                request.title().trim(),
                request.description().trim(),
                request.budget(),
                client
        );

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAvailableProjects() {
        return projectRepository
                .findByStatusOrderByCreatedAtDesc(ProjectStatus.OPEN)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        return ProjectResponse.from(getProject(id));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(String email) {
        AppUser user = getCurrentUser(email);

        if (user.getRole() == UserRole.CLIENT) {
            return projectRepository
                    .findByClientIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(ProjectResponse::from)
                    .toList();
        }

        if (user.getRole() == UserRole.FREELANCER) {
            return projectRepository
                    .findByFreelancerIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(ProjectResponse::from)
                    .toList();
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Arbitrators do not have project ownership");
    }

    @Transactional
    public ProjectResponse updateProject(
            Long id,
            UpdateProjectRequest request,
            String email) {

        AppUser user = getCurrentUser(email);
        requireRole(user, UserRole.CLIENT);

        Project project = getProject(id);
        requireOwner(project, user);

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only open projects can be edited");
        }

        project.updateDetails(
                request.title().trim(),
                request.description().trim(),
                request.budget()
        );

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProjectStatus(
            Long id,
            ProjectStatus newStatus,
            String email) {

        AppUser user = getCurrentUser(email);
        requireRole(user, UserRole.CLIENT);

        Project project = getProject(id);
        requireOwner(project, user);

        // The client may cancel an open project.
        if (project.getStatus() == ProjectStatus.OPEN
                && newStatus == ProjectStatus.CANCELLED) {

            project.updateStatus(ProjectStatus.CANCELLED);
            return ProjectResponse.from(project);
        }

        // All other lifecycle changes are managed internally
        // by application acceptance and escrow transactions.
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This project status cannot be changed manually. "
                        + "Use the application or escrow workflow."
        );
    }

    @Transactional
    public void deleteProject(Long id, String email) {
        AppUser user = getCurrentUser(email);
        requireRole(user, UserRole.CLIENT);

        Project project = getProject(id);
        requireOwner(project, user);

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only open projects can be deleted");
        }

        projectRepository.delete(project);
    }
}