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
        String name = account.getMarketingName() == null ? account.getName() : account.getMarketingName();

        return AccountResponse.builder()
                .id(account.getId())
                .name(name)
                .type(account.getType().name())
                .subtype(account.getSubType().name())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }
}
