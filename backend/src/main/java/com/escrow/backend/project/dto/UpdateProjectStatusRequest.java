package com.escrow.backend.project.dto;

import com.escrow.backend.project.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectStatusRequest(

        @NotNull
        ProjectStatus status

) {}
