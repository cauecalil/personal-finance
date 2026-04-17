package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Summary metrics for the selected period and optional account filter.")
public record GetDashboardMetricsResponse(
    @Schema(description = "Current balance for the selected account, or aggregated bank balances when no account is specified.", example = "12540.32", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal currentBalance,
    @Schema(description = "Total credited amount in the selected period.", example = "9300.50", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal totalIncome,
    @Schema(description = "Total debited amount in the selected period.", example = "4210.18", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal totalExpenses,
    @Schema(description = "ISO 4217 currency code used by these metrics.", example = "BRL", requiredMode = Schema.RequiredMode.REQUIRED)
    String currencyCode
) {}
