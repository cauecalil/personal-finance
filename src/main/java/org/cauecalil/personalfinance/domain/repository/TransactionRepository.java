package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Page<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable);
    Page<Transaction> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);
    boolean existsById(String id);
    void deleteByAccountId(String accountId);
}
