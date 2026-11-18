package com.escrow.backend.project.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlockchainConfirmationRequest(

        @NotBlank
        @Size(max = 100)
        String transactionHash

) {
}
