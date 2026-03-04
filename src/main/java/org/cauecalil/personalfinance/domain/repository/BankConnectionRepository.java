package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.BankConnection;

import java.util.Optional;

public interface BankConnectionRepository {
    BankConnection save(BankConnection bankConnection);
    Optional<BankConnection> findById(String id);
    void delete(String id);
}
