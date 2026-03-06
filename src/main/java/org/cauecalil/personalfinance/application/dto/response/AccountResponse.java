package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;
import org.cauecalil.personalfinance.domain.model.Account;

import java.math.BigDecimal;

@Builder
public record AccountResponse(
        String id,
        String name,
        String type,
        String subtype,
        BigDecimal balance,
        String currency
) {
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .subtype(account.getSubtype())
                .balance(account.getBalance())
                .currency(account.getCurrency().name())
                .build();
    }
}
