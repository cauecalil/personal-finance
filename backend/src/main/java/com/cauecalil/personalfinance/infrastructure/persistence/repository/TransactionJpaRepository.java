package com.cauecalil.personalfinance.infrastructure.persistence.repository;

import com.cauecalil.personalfinance.domain.repository.TransactionRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, String> {
    interface CashflowAggregationProjection {
        String getPeriodKey();
        BigDecimal getIncomeTotal();
        BigDecimal getExpensesTotal();
    }

    Page<TransactionJpaEntity> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable);
    Page<TransactionJpaEntity> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN ABS(COALESCE(t.amountInAccountCurrency, t.amount)) ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN ABS(COALESCE(t.amountInAccountCurrency, t.amount)) ELSE 0 END), 0) AS totalExpenses
        FROM TransactionJpaEntity t
        WHERE t.occurredAt BETWEEN :from AND :to
            AND (:accountId IS NULL OR t.account.id = :accountId)
            AND (:accountId IS NOT NULL OR COALESCE(t.movementClass, 'REGULAR') <> 'INTERNAL_TRANSFER')
    """)
    TransactionRepository.Metrics findMetrics(String accountId, Instant from, Instant to);

    @Query("""
        SELECT
            t.type AS type,
            COALESCE(root.descriptionTranslated, root.description, cat.descriptionTranslated, cat.description, 'Other') AS category,
            SUM(ABS(COALESCE(t.amountInAccountCurrency, t.amount))) AS total
        FROM TransactionJpaEntity t
        LEFT JOIN t.category cat
        LEFT JOIN CategoryJpaEntity root ON root.id = cat.rootCategoryId
        WHERE t.occurredAt BETWEEN :from AND :to AND (:accountId IS NULL OR t.account.id = :accountId)
            AND (:accountId IS NOT NULL OR COALESCE(t.movementClass, 'REGULAR') <> 'INTERNAL_TRANSFER')
        GROUP BY t.type, COALESCE(root.descriptionTranslated, root.description, cat.descriptionTranslated, cat.description, 'Other')
        ORDER BY t.type ASC, SUM(ABS(COALESCE(t.amountInAccountCurrency, t.amount))) DESC
    """)
    List<TransactionRepository.CategoryAggregation> findCategoryAggregations(String accountId, Instant from, Instant to);

    @Query(value = """
        SELECT
            x.periodKey AS periodKey,
            COALESCE(SUM(CASE WHEN x.type = 'CREDIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS incomeTotal,
            COALESCE(SUM(CASE WHEN x.type = 'DEBIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS expensesTotal
        FROM (
            SELECT
                FORMATDATETIME(t.occurred_at, 'yyyy-MM-dd', 'en', :timeZone) AS periodKey,
                t.type AS type,
                ABS(COALESCE(t.amount_in_account_currency, t.amount)) AS effectiveAmount
            FROM transactions t
            WHERE t.occurred_at BETWEEN :from AND :to
              AND (:accountId IS NULL OR t.account_id = :accountId)
              AND (:accountId IS NOT NULL OR COALESCE(t.movement_class, 'REGULAR') <> 'INTERNAL_TRANSFER')
        ) x
        GROUP BY x.periodKey
        ORDER BY x.periodKey
    """, nativeQuery = true)
    List<CashflowAggregationProjection> findDailyCashflowAggregations(String accountId, Instant from, Instant to, String timeZone);

    @Query(value = """
        SELECT
            x.periodKey AS periodKey,
            COALESCE(SUM(CASE WHEN x.type = 'CREDIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS incomeTotal,
            COALESCE(SUM(CASE WHEN x.type = 'DEBIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS expensesTotal
        FROM (
            SELECT
                FORMATDATETIME(t.occurred_at, 'YYYY-ww', 'en', :timeZone) AS periodKey,
                t.type AS type,
                ABS(COALESCE(t.amount_in_account_currency, t.amount)) AS effectiveAmount
            FROM transactions t
            WHERE t.occurred_at BETWEEN :from AND :to
              AND (:accountId IS NULL OR t.account_id = :accountId)
              AND (:accountId IS NOT NULL OR COALESCE(t.movement_class, 'REGULAR') <> 'INTERNAL_TRANSFER')
        ) x
        GROUP BY x.periodKey
        ORDER BY x.periodKey
    """, nativeQuery = true)
    List<CashflowAggregationProjection> findWeeklyCashflowAggregations(String accountId, Instant from, Instant to, String timeZone);

    @Query(value = """
        SELECT
            x.periodKey AS periodKey,
            COALESCE(SUM(CASE WHEN x.type = 'CREDIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS incomeTotal,
            COALESCE(SUM(CASE WHEN x.type = 'DEBIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS expensesTotal
        FROM (
            SELECT
                FORMATDATETIME(t.occurred_at, 'yyyy-MM', 'en', :timeZone) AS periodKey,
                t.type AS type,
                ABS(COALESCE(t.amount_in_account_currency, t.amount)) AS effectiveAmount
            FROM transactions t
            WHERE t.occurred_at BETWEEN :from AND :to
              AND (:accountId IS NULL OR t.account_id = :accountId)
              AND (:accountId IS NOT NULL OR COALESCE(t.movement_class, 'REGULAR') <> 'INTERNAL_TRANSFER')
        ) x
        GROUP BY x.periodKey
        ORDER BY x.periodKey
    """, nativeQuery = true)
    List<CashflowAggregationProjection> findMonthlyCashflowAggregations(String accountId, Instant from, Instant to, String timeZone);

    @Query(value = """
        SELECT
            x.periodKey AS periodKey,
            COALESCE(SUM(CASE WHEN x.type = 'CREDIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS incomeTotal,
            COALESCE(SUM(CASE WHEN x.type = 'DEBIT' THEN ABS(x.effectiveAmount) ELSE 0 END), 0) AS expensesTotal
        FROM (
            SELECT
                FORMATDATETIME(t.occurred_at, 'yyyy', 'en', :timeZone) AS periodKey,
                t.type AS type,
                ABS(COALESCE(t.amount_in_account_currency, t.amount)) AS effectiveAmount
            FROM transactions t
            WHERE t.occurred_at BETWEEN :from AND :to
              AND (:accountId IS NULL OR t.account_id = :accountId)
              AND (:accountId IS NOT NULL OR COALESCE(t.movement_class, 'REGULAR') <> 'INTERNAL_TRANSFER')
        ) x
        GROUP BY x.periodKey
        ORDER BY x.periodKey
    """, nativeQuery = true)
    List<CashflowAggregationProjection> findYearlyCashflowAggregations(String accountId, Instant from, Instant to, String timeZone);
}
