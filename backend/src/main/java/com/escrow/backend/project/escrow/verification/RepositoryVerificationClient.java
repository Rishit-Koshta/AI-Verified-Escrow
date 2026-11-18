package com.escrow.backend.project.escrow.verification;

import com.escrow.backend.project.escrow.dto.RepositoryVerificationRequest;
import com.escrow.backend.project.escrow.dto.RepositoryVerificationResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RepositoryVerificationClient {

    private final RestClient restClient;

    public RepositoryVerificationClient(
            RestClient.Builder builder,
            @Value("${ai.verification.url}") String verificationUrl
    ) {
        this.restClient = builder
                .baseUrl(verificationUrl)
                .build();
    }

    public RepositoryVerificationResponse verify(
            RepositoryVerificationRequest request
    ) {
        RepositoryVerificationResponse response = restClient.post()
                .uri("/api/verify/repository")
                .body(request)
                .retrieve()
                .body(RepositoryVerificationResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "AI verification service returned an empty response"
            );
        }

        return response;
    }
}
