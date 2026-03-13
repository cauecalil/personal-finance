package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void saveAll(List<Account> accounts);
    Optional<Account> findById(String id);
    List<Account> findAll();
    void delete(String id);
    void deleteAll();
}
