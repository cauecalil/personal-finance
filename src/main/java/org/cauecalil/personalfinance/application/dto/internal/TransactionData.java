package org.cauecalil.personalfinance.application.dto.internal;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TransactionData(
        String id,
        String description,
        BigDecimal amount,
        String type,
        String category,
        Instant occurredAt,
        String currencyCode
) {}
