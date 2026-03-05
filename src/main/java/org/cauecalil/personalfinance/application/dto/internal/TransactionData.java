package org.cauecalil.personalfinance.application.dto.internal;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record TransactionData(
        String pluggyTransactionId,
        String description,
        BigDecimal amount,
        String type,
        LocalDate date,
        String category,
        String currencyCode
) {}
