package com.example.email;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class HttpEmailClient implements EmailClient {
    // external dependency boundary

    private final RestClient restClient;

    public HttpEmailClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://example-email-api.dev")
                .build();
    }

    @Override
    public void sendWelcomeEmail(String email) {
        restClient.post()
                .uri("/send/welcome")
                .body(Map.of("email", email))
                .retrieve()
                .toBodilessEntity();
    }
}