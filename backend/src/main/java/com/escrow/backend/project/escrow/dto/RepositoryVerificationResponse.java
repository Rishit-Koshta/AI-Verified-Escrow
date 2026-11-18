
package com.escrow.backend.project.escrow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RepositoryVerificationResponse(

        String repository,
        String verdict,
        Integer score,
        String summary,

        @JsonProperty("completed_requirements")
        List<String> completedRequirements,

        @JsonProperty("missing_requirements")
        List<String> missingRequirements,

        List<String> evidence,
        List<String> limitations

) {
}
