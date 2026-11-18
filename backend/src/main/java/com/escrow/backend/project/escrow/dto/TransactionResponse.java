package com.escrow.backend.project.escrow.dto;

import com.escrow.backend.project.escrow.transaction.EscrowTransaction;
import com.escrow.backend.project.escrow.transaction.TransactionStatus;
import com.escrow.backend.project.escrow.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

        Long id,
        Long escrowId,

        TransactionType type,
        TransactionStatus status,

        BigDecimal amount,
        String currency,

        String transactionHash,
        String fromAddress,
        String toAddress,

        Long blockNumber,
        String failureReason,

        LocalDateTime createdAt,
        LocalDateTime submittedAt,
        LocalDateTime confirmedAt

) {

    public static TransactionResponse from(EscrowTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getEscrow().getId(),

                transaction.getType(),
                transaction.getStatus(),

                transaction.getAmount(),
                transaction.getCurrency(),

                transaction.getTransactionHash(),
                transaction.getFromAddress(),
                transaction.getToAddress(),

                transaction.getBlockNumber(),
                transaction.getFailureReason(),

                transaction.getCreatedAt(),
                transaction.getSubmittedAt(),
                transaction.getConfirmedAt()
        );
    }
}
