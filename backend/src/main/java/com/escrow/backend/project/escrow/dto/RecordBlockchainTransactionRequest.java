package com.escrow.backend.project.escrow.dto;

import com.escrow.backend.project.escrow.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record RecordBlockchainTransactionRequest(

        @NotNull
        TransactionType type,

        @NotBlank
        @Pattern(regexp = "^0x[a-fA-F0-9]{64}$")
        String transactionHash,

        @NotBlank
        @Pattern(regexp = "^0x[a-fA-F0-9]{40}$")
        String walletAddress,

        @NotBlank
        @Pattern(regexp = "^0x[a-fA-F0-9]{40}$")
        String contractAddress,

        @NotNull
        Long chainId,

        @NotNull
        @Positive
        Long blockNumber
) {
}