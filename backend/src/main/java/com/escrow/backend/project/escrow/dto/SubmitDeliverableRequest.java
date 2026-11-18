
package com.escrow.backend.project.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SubmitDeliverableRequest(

        @NotBlank(message = "Description is required")
        @Size(max = 1500, message = "Description cannot exceed 1500 characters")
        String description,

        @NotBlank(message = "File URL is required")
        @Size(max = 1000, message = "File URL cannot exceed 1000 characters")
        String fileUrl,

        @NotBlank(message = "GitHub repository URL is required")
        @Size(max = 300, message = "Repository URL cannot exceed 300 characters")
        String repoUrl,

        @NotBlank(message = "Task description is required")
        @Size(min = 10, max = 5000, message = "Task description must contain 10-5000 characters")
        String taskDescription,

        @NotEmpty(message = "At least one acceptance criterion is required")
        @Size(max = 15, message = "Maximum 15 acceptance criteria allowed")
        List<@NotBlank @Size(max = 500) String> acceptanceCriteria

) {
}

