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
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private Instant occurredAt;
}
