package com.escrow.backend.project.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WalletRequest(

        @NotBlank(message = "Wallet address is required")
        @Pattern(
                regexp = "^0x[a-fA-F0-9]{40}$",
                message = "Invalid EVM wallet address"
        )
        String walletAddress

) {
}
