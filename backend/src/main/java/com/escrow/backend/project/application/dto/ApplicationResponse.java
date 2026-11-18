package com.escrow.backend.project.application.dto;

import com.escrow.backend.project.application.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long projectId,
        String projectTitle,
        Long freelancerId,
        String freelancerName,
        String freelancerEmail,
        String proposal,
        BigDecimal proposedBudget,
        Integer estimatedDays,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
    public static ApplicationResponse from(ProjectApplication app) {
        return new ApplicationResponse(
                app.getId(),
                app.getProject().getId(),
                app.getProject().getTitle(),
                app.getFreelancer().getId(),
                app.getFreelancer().getName(),
                app.getFreelancer().getEmail(),
                app.getProposal(),
                app.getProposedBudget(),
                app.getEstimatedDays(),
                app.getStatus(),
                app.getAppliedAt()
        );
    }
}
