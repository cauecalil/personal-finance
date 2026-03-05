package org.cauecalil.personalfinance.domain.model.valueobject;

import java.math.BigDecimal;

public enum TransactionType {
    CREDIT,
    DEBIT;

    public static TransactionType from(String type, BigDecimal amount) {
        return switch (type) {
            case "CREDIT" -> CREDIT;
            case "DEBIT" -> DEBIT;
            default -> amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.CREDIT : TransactionType.DEBIT;
        };
    }
}
