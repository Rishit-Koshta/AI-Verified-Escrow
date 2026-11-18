package com.escrow.backend.project.escrow.dto;

import com.escrow.backend.project.escrow.Escrow;
import com.escrow.backend.project.escrow.EscrowStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EscrowResponse(

        Long id,
        Long projectId,
        String projectTitle,

        Long clientId,
        String clientName,

        Long freelancerId,
        String freelancerName,

        BigDecimal amount,
        String currency,
        EscrowStatus status,

        String contractAddress,
        Long chainId,

        String depositTransactionHash,
        String releaseTransactionHash,
        String refundTransactionHash,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static EscrowResponse from(Escrow escrow) {
        return new EscrowResponse(
                escrow.getId(),
                escrow.getProject().getId(),
                escrow.getProject().getTitle(),

                escrow.getClient().getId(),
                escrow.getClient().getName(),

                escrow.getFreelancer().getId(),
                escrow.getFreelancer().getName(),

                escrow.getAmount(),
                escrow.getCurrency(),
                escrow.getStatus(),

                escrow.getContractAddress(),
                escrow.getChainId(),

                escrow.getDepositTransactionHash(),
                escrow.getReleaseTransactionHash(),
                escrow.getRefundTransactionHash(),

                escrow.getCreatedAt(),
                escrow.getUpdatedAt()
        );
    }
}
