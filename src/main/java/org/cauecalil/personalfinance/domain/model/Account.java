package org.cauecalil.personalfinance.domain.model;

import lombok.*;
import org.cauecalil.personalfinance.domain.model.valueobject.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {
    private String id;
    private Long bankConnectionId;
    private String name;
    private String type;
    private String subtype;
    private BigDecimal balance;
    private Currency currency;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void updateBalance(BigDecimal amount) {
        this.balance = amount;
        this.updatedAt = LocalDateTime.now();
    }
}
