package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void saveAll(List<Account> accounts);
    Optional<Account> findById(String id);
    List<Account> findAll();
    BigDecimal sumBalancesByType(AccountType type);
    void deleteAll();
}
