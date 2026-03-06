package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;
import org.cauecalil.personalfinance.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TransactionResponse(
        String id,
        String description,
        BigDecimal amount,
        String type,
        Instant date,
        String category,
        String currency
) {
    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .date(transaction.getOccurredAt())
                .category(transaction.getCategory())
                .currency("BRL")
                .build();
    }
}
