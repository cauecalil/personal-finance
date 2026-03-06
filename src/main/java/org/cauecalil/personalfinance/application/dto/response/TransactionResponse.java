package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;
import org.cauecalil.personalfinance.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Builder
public record TransactionResponse(
        String id,
        String description,
        BigDecimal amount,
        String type,
        String date,
        String category,
        String currency
) {
    public static TransactionResponse from(Transaction transaction, ZoneId zoneId) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .date(LocalDateTime.ofInstant(transaction.getOccurredAt(), zoneId).toString())
                .category(transaction.getCategory())
                .currency("BRL")
                .build();
    }
}
