package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record GetDashboardCashflowResponse(
    Granularity granularity,
    List<Point> points
) {
    public enum Granularity {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    @Builder
    public record Point (
            Instant periodStart,
            Instant periodEnd,
            BigDecimal incomeTotal,
            BigDecimal expensesTotal
    ) {}
}
