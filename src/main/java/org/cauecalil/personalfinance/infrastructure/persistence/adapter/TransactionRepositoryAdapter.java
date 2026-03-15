package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.TransactionMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.AccountJpaRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.CategoryJpaRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.temporal.IsoFields;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository transactionJpaRepository;
    private final AccountJpaRepository accountJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public void saveAll(List<Transaction> transactions) {
        List<TransactionJpaEntity> entities = transactions.stream()
                .map(transaction -> {
                    AccountJpaEntity accountRef = accountJpaRepository.getReferenceById(transaction.getAccountId());
                    CategoryJpaEntity categoryRef = categoryJpaRepository.getReferenceById(transaction.getCategoryId());
                    return TransactionMapper.toEntity(transaction, accountRef, categoryRef);
                })
                .toList();

        transactionJpaRepository.saveAll(entities);
    }

    @Override
    public Page<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable) {
        Page<TransactionJpaEntity> page = transactionJpaRepository.findByAccountIdAndOccurredAtBetween(accountId, from, to, pageable);
        return page.map(TransactionMapper::toDomain);
    }

    @Override
    public Page<Transaction> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable) {
        return transactionJpaRepository.findByOccurredAtBetween(from, to, pageable)
                .map(TransactionMapper::toDomain);
    }

    @Override
    public Metrics findMetrics(String accountId, Instant from, Instant to) {
        return transactionJpaRepository.findMetrics(accountId, from, to);
    }

    @Override
    public List<CategoryAggregation> findCategoryAggregations(String accountId, Instant from, Instant to) {
        return transactionJpaRepository.findCategoryAggregations(accountId, from, to);
    }

    @Override
    public List<CashflowAggregation> findCashflowAggregations(
            String accountId,
            Instant from,
            Instant to,
            CashflowGranularity granularity,
            ZoneId zoneId
    ) {
        List<TransactionJpaRepository.CashflowAggregationProjection> aggregations = switch (granularity) {
            case DAILY -> transactionJpaRepository.findDailyCashflowAggregations(accountId, from, to, zoneId.getId());
            case WEEKLY -> transactionJpaRepository.findWeeklyCashflowAggregations(accountId, from, to, zoneId.getId());
            case MONTHLY -> transactionJpaRepository.findMonthlyCashflowAggregations(accountId, from, to, zoneId.getId());
            case YEARLY -> transactionJpaRepository.findYearlyCashflowAggregations(accountId, from, to, zoneId.getId());
        };

        return aggregations.stream()
                .map(aggregation -> new CashflowAggregation(
                        toPeriodStart(aggregation.getPeriodKey(), granularity, zoneId),
                        aggregation.getIncomeTotal(),
                        aggregation.getExpensesTotal()
                ))
                .toList();
    }

    private Instant toPeriodStart(String periodKey, CashflowGranularity granularity, ZoneId zoneId) {
        return switch (granularity) {
            case DAILY -> LocalDate.parse(periodKey).atStartOfDay(zoneId).toInstant();
            case WEEKLY -> {
                String[] parts = periodKey.split("-");
                int weekBasedYear = Integer.parseInt(parts[0]);
                int week = Integer.parseInt(parts[1]);

                LocalDate weekStart = LocalDate.of(weekBasedYear, 1, 4)
                        .with(IsoFields.WEEK_BASED_YEAR, weekBasedYear)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                        .with(DayOfWeek.MONDAY);

                yield weekStart.atStartOfDay(zoneId).toInstant();
            }
            case MONTHLY -> YearMonth.parse(periodKey).atDay(1).atStartOfDay(zoneId).toInstant();
            case YEARLY -> Year.parse(periodKey).atDay(1).atStartOfDay(zoneId).toInstant();
        };
    }

    @Override
    public void deleteAll() {
        transactionJpaRepository.deleteAll();
    }
}
