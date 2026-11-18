package com.escrow.backend.project.escrow.transaction;

import com.escrow.backend.project.escrow.Escrow;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "escrow_transactions",
        indexes = {
                @Index(name = "idx_transaction_escrow", columnList = "escrow_id"),
                @Index(name = "idx_transaction_status", columnList = "status")
        }
)
public class EscrowTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escrow_id", nullable = false)
    private Escrow escrow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Column(length = 100)
    private String transactionHash;

    @Column(length = 100)
    private String fromAddress;

    @Column(length = 100)
    private String toAddress;

    private Long blockNumber;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    private LocalDateTime confirmedAt;

    protected EscrowTransaction() {
    }

    public EscrowTransaction(
            Escrow escrow,
            TransactionType type,
            BigDecimal amount,
            String currency
    ) {
        this.escrow = escrow;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = TransactionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markSubmitted(
            String transactionHash,
            String fromAddress,
            String toAddress
    ) {
        this.transactionHash = transactionHash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.status = TransactionStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void markConfirmed(Long blockNumber) {
        this.status = TransactionStatus.CONFIRMED;
        this.blockNumber = blockNumber;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markFailed(String failureReason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = failureReason;
    }

    public Long getId() {
        return id;
    }

    public Escrow getEscrow() {
        return escrow;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
}
