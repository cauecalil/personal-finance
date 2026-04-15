package com.cauecalil.personalfinance.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.heartbeat")
public record HeartbeatProperties(
        boolean enabled,
        @Min(5000) long timeoutMs,
        @Min(1000) long monitorDelayMs,
        @Min(0) long startupGraceMs
) {}
