
package com.escrow.backend.project.escrow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RepositoryVerificationRequest(

        @JsonProperty("repo_url")
        String repoUrl,

        @JsonProperty("task_description")
        String taskDescription,

        @JsonProperty("acceptance_criteria")
        List<String> acceptanceCriteria

) {
}
