package com.example.retry;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component("retryLogger")
public class RetryLoggingListener implements RetryListener {
    private static final Logger log = LoggerFactory.getLogger("JOBS");
    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable
    ) {

        log.warn("Retry attempt #{} due to: {}",
                context.getRetryCount(),
                throwable.getMessage());
    }
}
