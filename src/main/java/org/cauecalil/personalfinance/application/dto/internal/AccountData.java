package org.cauecalil.personalfinance.application.dto.internal;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountData(
        String pluggyAccountId,
        String name,
        String type,
        String subtype,
        BigDecimal balance,
        String currencyCode
) { }
