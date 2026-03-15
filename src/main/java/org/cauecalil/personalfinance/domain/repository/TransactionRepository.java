package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository {
    record Metrics(BigDecimal totalIncome, BigDecimal totalExpenses) {}

    void saveAll(List<Transaction> transactions);
    Page<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable);
    Page<Transaction> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);
    Metrics findMetrics(String accountId, Instant from, Instant to);
    void deleteAll();
}
