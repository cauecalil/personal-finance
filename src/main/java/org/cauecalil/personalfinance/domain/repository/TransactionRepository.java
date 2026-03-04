package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Transaction;

import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String id);
    void delete(String id);
}
