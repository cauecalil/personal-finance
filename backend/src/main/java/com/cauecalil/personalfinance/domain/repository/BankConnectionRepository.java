package com.cauecalil.personalfinance.domain.repository;

import com.cauecalil.personalfinance.domain.model.BankConnection;

import java.util.List;
import java.util.Optional;

public interface BankConnectionRepository {
    BankConnection save(BankConnection bankConnection);
    Optional<BankConnection> findById(Long id);
    List<BankConnection> findAll();
    boolean existsById(Long id);
    boolean existsByItemId(String itemId);
    void deleteById(Long id);
}
