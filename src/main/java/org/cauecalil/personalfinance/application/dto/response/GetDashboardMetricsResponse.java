package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GetDashboardMetricsResponse(
    BigDecimal currentBalance,
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    String currencyCode
) {}
