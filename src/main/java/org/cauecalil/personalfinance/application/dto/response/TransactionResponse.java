package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TransactionResponse(
        String id,
        String description,
        String currency,
        BigDecimal amount,
        BigDecimal amountInAccountCurrency,
        String type,
        Instant date,
        String category
) {
    public static TransactionResponse from(Transaction transaction, Category category) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .currency(transaction.getCurrency())
                .amount(transaction.getAmount())
                .amountInAccountCurrency(transaction.getAmountInAccountCurrency())
                .type(transaction.getType().name())
                .date(transaction.getOccurredAt())
                .category(category.getDisplayDescription())
                .build();
    }
}
