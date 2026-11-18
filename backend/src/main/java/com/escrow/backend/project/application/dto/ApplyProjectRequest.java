package com.escrow.backend.project.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ApplyProjectRequest(

        @NotBlank
        @Size(max = 2000)
        String proposal,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 13, fraction = 2)
        BigDecimal proposedBudget,

        @NotNull
        @Min(1)
        @Max(365)
        Integer estimatedDays

) {}
