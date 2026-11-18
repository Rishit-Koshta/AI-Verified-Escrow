package com.escrow.backend.project.application;

import com.escrow.backend.project.Project;
import com.escrow.backend.project.auth.AppUser;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "project_applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_freelancer",
                columnNames = {"project_id", "freelancer_id"}
        )
)
public class ProjectApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private AppUser freelancer;

    @Column(nullable = false, length = 2000)
    private String proposal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal proposedBudget;

    @Column(nullable = false)
    private Integer estimatedDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    protected ProjectApplication() {
    }

    public ProjectApplication(
            Project project,
            AppUser freelancer,
            String proposal,
            BigDecimal proposedBudget,
            Integer estimatedDays) {

        this.project = project;
        this.freelancer = freelancer;
        this.proposal = proposal;
        this.proposedBudget = proposedBudget;
        this.estimatedDays = estimatedDays;
        this.status = ApplicationStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = ApplicationStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public AppUser getFreelancer() {
        return freelancer;
    }

    public String getProposal() {
        return proposal;
    }

    public BigDecimal getProposedBudget() {
        return proposedBudget;
    }

    public Integer getEstimatedDays() {
        return estimatedDays;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
}
