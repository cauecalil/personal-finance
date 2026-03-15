package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, String> {
    Page<TransactionJpaEntity> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable);
    Page<TransactionJpaEntity> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);

    @Query("""
        SELECT
            SUM(CASE WHEN type = 'CREDIT' THEN COALESCE(amountInAccountCurrency, amount) ELSE 0 END) AS totalIncome,
            SUM(CASE WHEN type = 'DEBIT' THEN COALESCE(amountInAccountCurrency, amount) ELSE 0 END) AS totalExpenses
        FROM TransactionJpaEntity
        WHERE occurredAt BETWEEN :from AND :to
        AND (:accountId IS NULL OR account.id = :accountId)
    """)
    TransactionRepository.Metrics findMetrics(String accountId, Instant from, Instant to);
}
