package com.example.jobs;

import com.example.shared.SharedLinkRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
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

    private final SharedLinkCleanupExecutor executor;

    public SharedLinkCleanupJob(SharedLinkRepository repo, SharedLinkCleanupExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanupExpiredWrapper() {
        try {
            executor.cleanupExpiredWithRetry();
        } catch (BadSqlGrammarException e) {
            log.debug("SharedLink cleanup skipped: table not created (dev mode)");
        }
    }


}
