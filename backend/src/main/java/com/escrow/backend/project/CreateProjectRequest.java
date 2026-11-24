package com.escrow.backend.project;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProjectRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Budget is required")
        @DecimalMin(value = "0.0001", message = "Budget must be greater than zero")
        @Digits(integer = 13, fraction = 6, message = "Budget format invalid (up to 6 decimal places)")
        BigDecimal budget

) {
}
