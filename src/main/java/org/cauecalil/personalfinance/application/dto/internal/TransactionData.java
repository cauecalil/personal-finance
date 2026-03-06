package org.cauecalil.personalfinance.application.dto.internal;

import lombok.Builder;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TransactionData(
        String id,
        String description,
        String currency,
        BigDecimal amount,
        BigDecimal amountInAccountCurrency,
        TransactionType type,
        String category,
        Instant occurredAt
) {}
