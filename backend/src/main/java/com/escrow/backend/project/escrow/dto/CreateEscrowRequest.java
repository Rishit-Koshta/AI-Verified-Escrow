package com.escrow.backend.project.escrow.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateEscrowRequest(

        @NotNull(message = "Project ID is required")
        @Positive(message = "Project ID must be positive")
        Long projectId,

        @NotBlank(message = "Currency is required")
        String currency

) {
}
