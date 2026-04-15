package com.cauecalil.personalfinance.presentation.browser;

import com.cauecalil.personalfinance.config.HeartbeatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatBrowserLifecycle {
    private final HeartbeatProperties heartbeatProperties;
    private final ConfigurableApplicationContext applicationContext;
    private final AtomicLong lastHeartbeatEpochMs = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final long startupEpochMs = System.currentTimeMillis();

    public void markAlive() {
        lastHeartbeatEpochMs.set(System.currentTimeMillis());
    }

    @Scheduled(fixedDelayString = "${app.heartbeat.monitor-delay-ms}")
    public void shutdownWhenTimedOut() {
        if (!heartbeatProperties.enabled()) return;

        long uptimeMs = System.currentTimeMillis() - startupEpochMs;
        if (uptimeMs < heartbeatProperties.startupGraceMs()) return;

        long inactiveMs = Math.max(0, System.currentTimeMillis() - lastHeartbeatEpochMs.get());
        if (inactiveMs < heartbeatProperties.timeoutMs()) return;
        if (!shuttingDown.compareAndSet(false, true)) return;

        log.info("Heartbeat timeout reached ({} ms). Shutting down application.", inactiveMs);
        Thread shutdownThread = new Thread(() -> {
            int exitCode = SpringApplication.exit(applicationContext, () -> 0);
            System.exit(exitCode);
        }, "heartbeat-shutdown");
        shutdownThread.setDaemon(false);
        shutdownThread.start();
    }
}
