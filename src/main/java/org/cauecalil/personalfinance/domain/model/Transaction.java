package org.cauecalil.personalfinance.domain.model;

import lombok.*;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transaction {
    private String id;
    private String accountId;
    private String description;
    private String currency;
    private BigDecimal amount;
    private BigDecimal amountInAccountCurrency;
    private TransactionType type;
    private String categoryId;
    private Instant occurredAt;
}
