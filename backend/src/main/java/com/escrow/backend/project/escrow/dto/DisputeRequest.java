package com.escrow.backend.project.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisputeRequest(

        @NotBlank(message = "Dispute reason is required")
        @Size(max = 1500, message = "Dispute reason cannot exceed 1500 characters")
        String reason

) {
}
