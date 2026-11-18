package com.escrow.backend.project.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProjectRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 2000)
        String description,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 13, fraction = 2)
        BigDecimal budget

) {}
