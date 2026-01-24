package com.example.jobs;

import com.example.shared.SharedLinkRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

@Component
public class SharedLinkCleanupJob {

    private static final Logger log = LoggerFactory.getLogger("JOBS");
    private final SharedLinkRepository repo;

    public SharedLinkCleanupJob(SharedLinkRepository repo) {
        this.repo = repo;
    }
    @Transactional
    @Scheduled(fixedDelay = 60_000) // every 60s
    @Retryable(
            retryFor = { TransientDataAccessException.class, SQLException.class },
            noRetryFor = { IllegalArgumentException.class,  org.springframework.jdbc.BadSqlGrammarException.class },
            maxAttempts = 5,
            backoff = @Backoff(
            delay = 1000,
            maxDelay = 30000,
            multiplier = 2,
            random = true  // jitter
    ) // exponential
    )
    public void cleanupExpired() {
        MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8)); // proper logging
        try {
            int deleted = repo.deleteByExpiresAtBefore(Instant.now());
            if (deleted > 0) {
                log.info("Cleanup shared links: deleted={}", deleted);
            }
        } finally {
            MDC.clear();
        }
    }

    @Recover
    public void recover(Exception e) {
        log.error("SharedLink cleanup FAILED after retries", e);
         // add alert/metric sending later
    }
}
