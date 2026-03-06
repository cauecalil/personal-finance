package org.cauecalil.personalfinance.application.dto.internal;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountData(
        String id,
        String name,
        String marketingName,
        String type,
        String subType,
        String number,
        String owner,
        String taxNumber,
        BigDecimal balance,
        String currency
) {}
