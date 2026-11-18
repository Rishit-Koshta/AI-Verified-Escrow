package com.escrow.backend.project;

import com.escrow.backend.project.auth.AppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal budget;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private AppUser client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id")
    private AppUser freelancer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status = ProjectStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Project() {
    }

    public Project(
            String title,
            String description,
            BigDecimal budget,
            AppUser client) {

        this.title = title;
        this.description = description;
        this.budget = budget;
        this.client = client;
        this.status = ProjectStatus.OPEN;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateDetails(
            String title,
            String description,
            BigDecimal budget) {

        this.title = title;
        this.description = description;
        this.budget = budget;
    }

    public void assignFreelancer(AppUser freelancer) {
        this.freelancer = freelancer;
        this.status = ProjectStatus.ASSIGNED;
    }

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public AppUser getClient() {
        return client;
    }

    public AppUser getFreelancer() {
        return freelancer;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

