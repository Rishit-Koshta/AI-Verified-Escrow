package com.escrow.backend.project.escrow.deliverable;


import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.escrow.Escrow;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "deliverables",
        indexes = {
                @Index(name = "idx_deliverable_escrow", columnList = "escrow_id"),
                @Index(name = "idx_deliverable_status", columnList = "status")
        }
)
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escrow_id", nullable = false)
    private Escrow escrow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private AppUser submittedBy;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Column(length = 300)
    private String repoUrl;

    @Column(length = 30)
    private String verificationStatus = "PENDING";

    @Column(length = 30)
    private String aiVerdict;

    private Integer aiScore;

    @Column(length = 2000)
    private String aiSummary;

    @Lob
    private String verificationDetails;

    @Column(nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliverableStatus status = DeliverableStatus.SUBMITTED;

    @Column(length = 1500)
    private String clientFeedback;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    protected Deliverable() {
    }

    public Deliverable(
            Escrow escrow,
            AppUser submittedBy,
            String description,
            String fileUrl,
            Integer version
    ) {
        this.escrow = escrow;
        this.submittedBy = submittedBy;
        this.description = description;
        this.fileUrl = fileUrl;
        this.version = version;
        this.status = DeliverableStatus.SUBMITTED;
    }

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = DeliverableStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void requestRevision(String feedback) {
        this.status = DeliverableStatus.REVISION_REQUESTED;
        this.clientFeedback = feedback;
        this.reviewedAt = LocalDateTime.now();
    }

    public void markDisputed() {
        this.status = DeliverableStatus.DISPUTED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void setRepositoryUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public void setVerificationResult(
            String verdict,
            Integer score,
            String summary,
            String details
    ) {
        this.aiVerdict = verdict;
        this.aiScore = score;
        this.aiSummary = summary;
        this.verificationDetails = details;
        this.verificationStatus = "COMPLETED";
    }

    public void markVerificationFailed(String error) {
        this.verificationStatus = "FAILED";
        this.aiSummary = error;
    }

    public Long getId() {
        return id;
    }

    public Escrow getEscrow() {
        return escrow;
    }

    public AppUser getSubmittedBy() {
        return submittedBy;
    }

    public String getDescription() {
        return description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public String getAiVerdict() {
        return aiVerdict;
    }

    public Integer getAiScore() {
        return aiScore;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public String getVerificationDetails() {
        return verificationDetails;
    }

    public Integer getVersion() {
        return version;
    }

    public DeliverableStatus getStatus() {
        return status;
    }

    public String getClientFeedback() {
        return clientFeedback;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}