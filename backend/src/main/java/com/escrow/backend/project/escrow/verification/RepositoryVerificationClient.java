package com.escrow.backend.project.escrow.verification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.escrow.backend.project.escrow.dto.RepositoryVerificationRequest;
import com.escrow.backend.project.escrow.dto.RepositoryVerificationResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RepositoryVerificationClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RepositoryVerificationClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${ai.verification.url}") String verificationUrl) {

        this.restClient = builder
                .baseUrl(verificationUrl)
                .build();

        this.objectMapper = objectMapper;
    }

    public RepositoryVerificationResponse verify(
            RepositoryVerificationRequest request) {

        try {
            // Explicitly convert the DTO into JSON.
            String requestJson = objectMapper.writeValueAsString(request);

            RepositoryVerificationResponse response = restClient
                    .post()
                    .uri("/api/verify/repository")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(RepositoryVerificationResponse.class);

            if (response == null) {
                throw new IllegalStateException(
                        "AI verification service returned an empty response"
                );
            }

            return response;

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize AI verification request",
                    ex
            );

        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "AI service returned HTTP "
                            + ex.getStatusCode().value()
                            + ": "
                            + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }
}