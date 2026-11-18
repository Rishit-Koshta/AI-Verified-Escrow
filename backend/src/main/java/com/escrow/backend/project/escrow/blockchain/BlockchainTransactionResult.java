package com.escrow.backend.project.escrow.blockchain;

public record BlockchainTransactionResult(
        String transactionHash,
        BlockchainTransactionStatus status,
        Long blockNumber,
        String failureReason
) {
}
