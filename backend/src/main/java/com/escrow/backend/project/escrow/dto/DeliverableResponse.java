package com.escrow.backend.project.escrow.dto;

import com.escrow.backend.project.escrow.deliverable.Deliverable;
import com.escrow.backend.project.escrow.deliverable.DeliverableStatus;

import java.time.LocalDateTime;

public record DeliverableResponse(

        Long id,
        Long escrowId,
        Long submittedById,
        String submittedByName,

        String description,
        String fileUrl,
        String repoUrl,
        Integer version,

        DeliverableStatus status,
        String clientFeedback,

        String verificationStatus,
        String aiVerdict,
        Integer aiScore,
        String aiSummary,
        String verificationDetails,

        LocalDateTime submittedAt,
        LocalDateTime reviewedAt

) {

    public static DeliverableResponse from(Deliverable deliverable) {
        return new DeliverableResponse(
                deliverable.getId(),
                deliverable.getEscrow().getId(),
                deliverable.getSubmittedBy().getId(),
                deliverable.getSubmittedBy().getName(),

                deliverable.getDescription(),
                deliverable.getFileUrl(),
                deliverable.getRepoUrl(),
                deliverable.getVersion(),

                deliverable.getStatus(),
                deliverable.getClientFeedback(),

                deliverable.getVerificationStatus(),
                deliverable.getAiVerdict(),
                deliverable.getAiScore(),
                deliverable.getAiSummary(),
                deliverable.getVerificationDetails(),

                deliverable.getSubmittedAt(),
                deliverable.getReviewedAt()
        );
    }
}