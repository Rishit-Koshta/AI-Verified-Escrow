
package com.escrow.backend.project.escrow.dispute;

import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.escrow.Escrow;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escrow_id", nullable = false, unique = true)
    private Escrow escrow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by_id", nullable = false)
    private AppUser raisedBy;

    @Column(nullable = false, length = 1500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status = DisputeStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private DisputeResolution resolution;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    protected Dispute() {
    }

    public Dispute(Escrow escrow, AppUser raisedBy, String reason) {
        this.escrow = escrow;
        this.raisedBy = raisedBy;
        this.reason = reason;
        this.status = DisputeStatus.OPEN;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void resolve(DisputeResolution resolution) {
        if (this.status != DisputeStatus.OPEN) {
            throw new IllegalStateException("Dispute is already resolved");
        }

        this.resolution = resolution;
        this.status = DisputeStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Escrow getEscrow() {
        return escrow;
    }

    public AppUser getRaisedBy() {
        return raisedBy;
    }

    public String getReason() {
        return reason;
    }

    public DisputeStatus getStatus() {
        return status;
    }

    public DisputeResolution getResolution() {
        return resolution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
