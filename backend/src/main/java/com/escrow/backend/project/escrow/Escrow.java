
package com.escrow.backend.project.escrow;

import com.escrow.backend.project.Project;
import com.escrow.backend.project.auth.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "escrows",
        indexes = {
                @Index(name = "idx_escrow_client", columnList = "client_id"),
                @Index(name = "idx_escrow_freelancer", columnList = "freelancer_id"),
                @Index(name = "idx_escrow_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_escrow_project",
                        columnNames = "project_id"
                )
        }
)
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private AppUser client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private AppUser freelancer;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EscrowStatus status = EscrowStatus.CREATED;

    // Blockchain information
    @Column(length = 100)
    private String contractAddress;

    private Long chainId;

    @Column(length = 100)
    private String depositTransactionHash;

    @Column(length = 100)
    private String releaseTransactionHash;

    @Column(length = 100)
    private String refundTransactionHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Escrow() {
    }

    public Escrow(
            Project project,
            AppUser client,
            AppUser freelancer,
            BigDecimal amount,
            String currency
    ) {
        this.project = project;
        this.client = client;
        this.freelancer = freelancer;
        this.amount = amount;
        this.currency = currency;
        this.status = EscrowStatus.CREATED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(EscrowStatus status) {
        this.status = status;
    }

    public void setDepositTransaction(String transactionHash) {
        this.depositTransactionHash = transactionHash;
    }

    public void setReleaseTransaction(String transactionHash) {
        this.releaseTransactionHash = transactionHash;
    }

    public void setRefundTransaction(String transactionHash) {
        this.refundTransactionHash = transactionHash;
    }

    public void setBlockchainDetails(String contractAddress, Long chainId) {
        this.contractAddress = contractAddress;
        this.chainId = chainId;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public AppUser getClient() {
        return client;
    }

    public AppUser getFreelancer() {
        return freelancer;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public EscrowStatus getStatus() {
        return status;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public Long getChainId() {
        return chainId;
    }

    public String getDepositTransactionHash() {
        return depositTransactionHash;
    }

    public String getReleaseTransactionHash() {
        return releaseTransactionHash;
    }

    public String getRefundTransactionHash() {
        return refundTransactionHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
