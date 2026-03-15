package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.GetDashboardCashflowResponse;
import org.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetDashboardCashflowUseCase {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetDashboardCashflowResponse execute(String accountId, Instant fromInstant, Instant toInstant, ZoneId zoneId) {
        if (fromInstant.isAfter(toInstant)) {
            throw new FromDateAfterToDateException();
        }

        Optional<Account> account = Optional.ofNullable(accountId).flatMap(accountRepository::findById);

        if (accountId != null && account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        TransactionRepository.CashflowGranularity granularity = resolveGranularity(fromInstant, toInstant);

        List<TransactionRepository.CashflowAggregation> aggregations = transactionRepository
                .findCashflowAggregations(accountId, fromInstant, toInstant, granularity, zoneId);

        Map<Instant, TransactionRepository.CashflowAggregation> byPeriod = new HashMap<>();
        for (TransactionRepository.CashflowAggregation aggregation : aggregations) {
            byPeriod.put(aggregation.periodStart(), aggregation);
        }

        List<GetDashboardCashflowResponse.Point> points = buildContinuousTimeline(
                byPeriod,
                granularity,
                fromInstant,
                toInstant,
                zoneId
        );

        return GetDashboardCashflowResponse.builder()
                .granularity(mapGranularity(granularity))
                .points(points)
                .build();
    }

    private TransactionRepository.CashflowGranularity resolveGranularity(Instant fromInstant, Instant toInstant) {
        long days = ChronoUnit.DAYS.between(fromInstant, toInstant) + 1;

        if (days <= 31) {
            return TransactionRepository.CashflowGranularity.DAILY;
        }

        if (days <= 180) {
            return TransactionRepository.CashflowGranularity.WEEKLY;
        }

        if (days <= 730) {
            return TransactionRepository.CashflowGranularity.MONTHLY;
        }

        return TransactionRepository.CashflowGranularity.YEARLY;
    }

    private List<GetDashboardCashflowResponse.Point> buildContinuousTimeline(
            Map<Instant, TransactionRepository.CashflowAggregation> byPeriod,
            TransactionRepository.CashflowGranularity granularity,
            Instant fromInstant,
            Instant toInstant,
            ZoneId zoneId
    ) {
        Instant cursor = bucketStart(fromInstant, granularity, zoneId);
        Instant rangeEnd = bucketStart(toInstant, granularity, zoneId);

        List<GetDashboardCashflowResponse.Point> points = new java.util.ArrayList<>();

        while (!cursor.isAfter(rangeEnd)) {
            Instant next = nextBucketStart(cursor, granularity, zoneId);
            Instant periodEnd = next.minusNanos(1);
            if (periodEnd.isAfter(toInstant)) {
                periodEnd = toInstant;
            }

            TransactionRepository.CashflowAggregation aggregation = byPeriod.get(cursor);
            BigDecimal incomeTotal = aggregation != null ? aggregation.incomeTotal().abs() : BigDecimal.ZERO;
            BigDecimal expensesTotal = aggregation != null ? aggregation.expensesTotal().abs() : BigDecimal.ZERO;

            points.add(GetDashboardCashflowResponse.Point.builder()
                    .periodStart(cursor)
                    .periodEnd(periodEnd)
                    .incomeTotal(incomeTotal)
                    .expensesTotal(expensesTotal)
                    .build());

            cursor = next;
        }

        return points;
    }

    private Instant bucketStart(Instant instant, TransactionRepository.CashflowGranularity granularity, ZoneId zoneId) {
        ZonedDateTime dateTime = instant.atZone(zoneId);

        return switch (granularity) {
            case DAILY -> dateTime.truncatedTo(ChronoUnit.DAYS).toInstant();
            case WEEKLY -> dateTime
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant();
            case MONTHLY -> dateTime.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS).toInstant();
            case YEARLY -> dateTime.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS).toInstant();
        };
    }

    private Instant nextBucketStart(Instant start, TransactionRepository.CashflowGranularity granularity, ZoneId zoneId) {
        ZonedDateTime dateTime = start.atZone(zoneId);

        return switch (granularity) {
            case DAILY -> dateTime.plusDays(1).toInstant();
            case WEEKLY -> dateTime.plusWeeks(1).toInstant();
            case MONTHLY -> dateTime.plusMonths(1).toInstant();
            case YEARLY -> dateTime.plusYears(1).toInstant();
        };
    }

    private GetDashboardCashflowResponse.Granularity mapGranularity(TransactionRepository.CashflowGranularity granularity) {
        return switch (granularity) {
            case DAILY -> GetDashboardCashflowResponse.Granularity.DAILY;
            case WEEKLY -> GetDashboardCashflowResponse.Granularity.WEEKLY;
            case MONTHLY -> GetDashboardCashflowResponse.Granularity.MONTHLY;
            case YEARLY -> GetDashboardCashflowResponse.Granularity.YEARLY;
        };
    }
}
