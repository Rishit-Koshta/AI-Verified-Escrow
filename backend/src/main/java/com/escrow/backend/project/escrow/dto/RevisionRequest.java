package com.escrow.backend.project.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevisionRequest(

        @NotBlank(message = "Feedback is required")
        @Size(max = 1500, message = "Feedback cannot exceed 1500 characters")
        String feedback

) {
}
