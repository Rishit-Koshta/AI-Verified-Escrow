package com.escrow.backend.project.dto;

import com.escrow.backend.project.Project;
import com.escrow.backend.project.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String title,
        String description,
        BigDecimal budget,
        Long clientId,
        String clientName,
        Long freelancerId,
        String freelancerName,
        ProjectStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getBudget(),
                project.getClient().getId(),
                project.getClient().getName(),
                project.getFreelancer() == null
                        ? null : project.getFreelancer().getId(),
                project.getFreelancer() == null
                        ? null : project.getFreelancer().getName(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
