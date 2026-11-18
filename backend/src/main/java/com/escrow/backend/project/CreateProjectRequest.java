package com.escrow.backend.project;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProjectRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @NotBlank
        @Size(max = 100)
        String clientName,

        @NotBlank
        @Size(max = 100)
        String freelancerName,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal budget

) {
}
