package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String id);
    List<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to);
    boolean existsById(String id);
    void deleteByAccountId(String accountId);
}
