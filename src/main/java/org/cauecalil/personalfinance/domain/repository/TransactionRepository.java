package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public interface TransactionRepository {
    record Metrics(BigDecimal totalIncome, BigDecimal totalExpenses) {}
    record CategoryAggregation(TransactionType type, String category, BigDecimal total) {}
    record CashflowAggregation(Instant periodStart, BigDecimal incomeTotal, BigDecimal expensesTotal) {}

    enum CashflowGranularity {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    void saveAll(List<Transaction> transactions);
    Page<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable);
    Page<Transaction> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);
    Metrics findMetrics(String accountId, Instant from, Instant to);
    List<CategoryAggregation> findCategoryAggregations(String accountId, Instant from, Instant to);
    List<CashflowAggregation> findCashflowAggregations(String accountId, Instant from, Instant to, CashflowGranularity granularity, ZoneId zoneId);
    void deleteAll();
}
