package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Account;

import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(String id);
    void delete(String id);
}
