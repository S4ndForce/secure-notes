package com.example.jobs;

import com.example.email.EmailClient;
import com.example.idempotency.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailJobService {

    private static final Logger log = LoggerFactory.getLogger("JOBS");
    private final EmailClient emailClient;
    private final IdempotencyService idempotencyService;

    public EmailJobService(EmailClient emailClient, IdempotencyService idempotencyService) {
        this.emailClient = emailClient;
        this.idempotencyService = idempotencyService;
    }

    @Async("appExecutor")
    @Retryable(
            retryFor = {
                    RuntimeException.class
            },
            noRetryFor = {
                    IllegalArgumentException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2,
                    maxDelay = 15000,
                    random = true
            )
    )
    public void sendWelcomeEmail(String email) {

        String actionKey = "welcome-email:" + email;

        if (!idempotencyService.tryStart(actionKey)) {
            log.info("Skipping duplicate welcome email userId={}", email);
            return;
        }

        log.info("Sending welcome email userId={} email={}", email);

        // Simulate email send
        emailClient.sendWelcomeEmail(email);

        idempotencyService.markCompleted(actionKey);

        log.info("Welcome email completed userId={}", email);
    }



    @Recover
    public void recover(RuntimeException e, UUID userId, String email) {
        log.error(
                "Welcome email permanently failed userId={} email={}",
                userId,
                email,
                e
        );
        // later: persist failure, alert, or DLQ
    }
}
