package com.cauecalil.personalfinance.application.dto.response;

import com.cauecalil.personalfinance.domain.model.Category;
import com.cauecalil.personalfinance.domain.model.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Schema(description = "Represents a classified financial transaction enriched with category information.")
public record TransactionResponse(
    @Schema(description = "Unique transaction identifier from the data provider.", example = "trn_29f5d31d5f7a4a73", requiredMode = Schema.RequiredMode.REQUIRED)
        String id,

    @Schema(description = "Original transaction description from the statement.", example = "SUPERMERCADO EXTRA", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

    @Schema(description = "ISO 4217 currency code of the original transaction amount.", example = "BRL", requiredMode = Schema.RequiredMode.REQUIRED)
        String currency,

    @Schema(description = "Original signed amount of the transaction.", example = "-185.42", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

    @Schema(description = "Transaction amount converted to the account currency when available.", example = "-185.42", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        BigDecimal amountInAccountCurrency,

    @Schema(description = "Transaction type. Possible values: CREDIT, DEBIT.", example = "DEBIT", requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

    @Schema(description = "Transaction occurrence timestamp in ISO 8601 instant format.", example = "2026-04-12T18:21:59Z", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant date,

    @Schema(description = "Human-readable category assigned to the transaction.", example = "Groceries", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
