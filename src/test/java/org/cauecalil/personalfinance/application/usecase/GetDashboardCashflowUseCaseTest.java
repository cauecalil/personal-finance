package org.cauecalil.personalfinance.application.usecase;

import org.cauecalil.personalfinance.application.dto.response.GetDashboardCashflowResponse;
import org.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetDashboardCashflowUseCaseTest {
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void shouldFillMissingDailyPeriodsWithZero() {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-03T23:59:59Z");

        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.cashflowAggregations = List.of(
                new TransactionRepository.CashflowAggregation(
                        Instant.parse("2026-03-01T00:00:00Z"),
                        new BigDecimal("100.00"),
                        new BigDecimal("40.00")
                ),
                new TransactionRepository.CashflowAggregation(
                        Instant.parse("2026-03-03T00:00:00Z"),
                        new BigDecimal("35.50"),
                        new BigDecimal("10.10")
                )
        );

        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), transactionRepository);

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertEquals(GetDashboardCashflowResponse.Granularity.DAILY, response.granularity());
        assertEquals(3, response.points().size());

        assertEquals(new BigDecimal("100.00"), response.points().get(0).incomeTotal());
        assertEquals(new BigDecimal("40.00"), response.points().get(0).expensesTotal());

        assertEquals(BigDecimal.ZERO, response.points().get(1).incomeTotal());
        assertEquals(BigDecimal.ZERO, response.points().get(1).expensesTotal());

        assertEquals(new BigDecimal("35.50"), response.points().get(2).incomeTotal());
        assertEquals(new BigDecimal("10.10"), response.points().get(2).expensesTotal());
        assertEquals(UTC, transactionRepository.lastZoneId);
        assertEquals(from, transactionRepository.lastFrom);
        assertEquals(to, transactionRepository.lastTo);
    }

    @Test
    void shouldReturnAbsoluteTotalsWhenRepositoryReturnsSignedValues() {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-01T23:59:59Z");

        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.cashflowAggregations = List.of(
                new TransactionRepository.CashflowAggregation(
                        Instant.parse("2026-03-01T00:00:00Z"),
                        new BigDecimal("-250.00"),
                        new BigDecimal("-120.75")
                )
        );

        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), transactionRepository);

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertEquals(new BigDecimal("250.00"), response.points().getFirst().incomeTotal());
        assertEquals(new BigDecimal("120.75"), response.points().getFirst().expensesTotal());
    }

    @Test
    void shouldKeepDailyBucketAlignedWithRequestTimezoneAtMidnightBoundary() {
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        Instant from = Instant.parse("2026-03-01T03:00:00Z");
        Instant to = Instant.parse("2026-03-02T02:59:59Z");

        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.cashflowAggregations = List.of(
                new TransactionRepository.CashflowAggregation(
                        Instant.parse("2026-03-01T03:00:00Z"),
                        new BigDecimal("80.00"),
                        new BigDecimal("15.00")
                )
        );

        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), transactionRepository);

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, zoneId);

        assertEquals(GetDashboardCashflowResponse.Granularity.DAILY, response.granularity());
        assertEquals(1, response.points().size());
        assertEquals(Instant.parse("2026-03-01T03:00:00Z"), response.points().getFirst().periodStart());
        assertEquals(Instant.parse("2026-03-02T02:59:59Z"), response.points().getFirst().periodEnd());
    }

    @Test
    void shouldKeepDailyBucketAlignedWhenDstChangesDayLength() {
        ZoneId zoneId = ZoneId.of("America/New_York");
        Instant from = Instant.parse("2026-03-08T05:00:00Z");
        Instant to = Instant.parse("2026-03-09T03:59:59.999999999Z");

        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.cashflowAggregations = List.of(
                new TransactionRepository.CashflowAggregation(
                        Instant.parse("2026-03-08T05:00:00Z"),
                        new BigDecimal("42.00"),
                        new BigDecimal("7.00")
                )
        );

        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), transactionRepository);

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, zoneId);

        assertEquals(GetDashboardCashflowResponse.Granularity.DAILY, response.granularity());
        assertEquals(1, response.points().size());
        assertEquals(Instant.parse("2026-03-08T05:00:00Z"), response.points().getFirst().periodStart());
        assertEquals(Instant.parse("2026-03-09T03:59:59.999999999Z"), response.points().getFirst().periodEnd());
    }

    @Test
    void shouldChooseWeeklyGranularityForMediumRanges() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-01T00:00:00Z");

        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), transactionRepository);

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertEquals(GetDashboardCashflowResponse.Granularity.WEEKLY, response.granularity());
        assertEquals(TransactionRepository.CashflowGranularity.WEEKLY, transactionRepository.lastGranularity);
    }

    @Test
    void shouldChooseMonthlyGranularityForLongRanges() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");

        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), transactionRepository);

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertEquals(GetDashboardCashflowResponse.Granularity.MONTHLY, response.granularity());
        assertEquals(TransactionRepository.CashflowGranularity.MONTHLY, transactionRepository.lastGranularity);
    }

    @Test
    void shouldThrowWhenFromDateIsAfterToDate() {
        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(new FakeAccountRepository(), new FakeTransactionRepository());

        assertThrows(
                FromDateAfterToDateException.class,
                () -> useCase.execute(null, Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-03-01T00:00:00Z"), UTC)
        );
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        accountRepository.findByIdResult = null;
        GetDashboardCashflowUseCase useCase = new GetDashboardCashflowUseCase(accountRepository, new FakeTransactionRepository());

        assertThrows(
                AccountNotFoundException.class,
                () -> useCase.execute("missing", Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-03-01T23:59:59Z"), UTC)
        );
    }

    private static final class FakeAccountRepository implements AccountRepository {
        private Account findByIdResult;

        @Override
        public void saveAll(List<Account> accounts) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Account> findById(String id) {
            return Optional.ofNullable(findByIdResult);
        }

        @Override
        public List<Account> findAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal sumBalancesByType(AccountType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeTransactionRepository implements TransactionRepository {
        private List<CashflowAggregation> cashflowAggregations = List.of();
        private CashflowGranularity lastGranularity;
        private ZoneId lastZoneId;
        private Instant lastFrom;
        private Instant lastTo;

        @Override
        public void saveAll(List<Transaction> transactions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<Transaction> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Metrics findMetrics(String accountId, Instant from, Instant to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CategoryAggregation> findCategoryAggregations(String accountId, Instant from, Instant to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CashflowAggregation> findCashflowAggregations(
                String accountId,
                Instant from,
                Instant to,
                CashflowGranularity granularity,
                ZoneId zoneId
        ) {
            this.lastGranularity = granularity;
            this.lastZoneId = zoneId;
            this.lastFrom = from;
            this.lastTo = to;
            return cashflowAggregations;
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }
    }
}

