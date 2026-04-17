package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Schema(description = "Cashflow timeline including income and expenses grouped by calculated granularity.")
public record GetDashboardCashflowResponse(
    @Schema(description = "Aggregation granularity selected from the date range. Possible values: DAILY, WEEKLY, MONTHLY, YEARLY.", example = "MONTHLY", requiredMode = Schema.RequiredMode.REQUIRED)
    Granularity granularity,

    @ArraySchema(arraySchema = @Schema(description = "Continuous sequence of cashflow points covering the requested interval."), schema = @Schema(implementation = Point.class))
    List<Point> points
) {
    @Schema(description = "Timeline granularity enum.")
    public enum Granularity {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    @Builder
    @Schema(description = "Income and expense totals for a single period bucket.")
    public record Point (
            @Schema(description = "Inclusive start instant of the period in ISO 8601 format.", example = "2026-04-01T00:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
            Instant periodStart,

            @Schema(description = "Inclusive end instant of the period in ISO 8601 format.", example = "2026-04-30T23:59:59.999999999Z", requiredMode = Schema.RequiredMode.REQUIRED)
            Instant periodEnd,

            @Schema(description = "Total credited amount for the period.", example = "5120.00", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal incomeTotal,

            @Schema(description = "Total debited amount for the period.", example = "3890.35", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal expensesTotal
    ) {}
}
