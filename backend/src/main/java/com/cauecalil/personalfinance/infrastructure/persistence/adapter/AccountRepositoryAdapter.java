package com.cauecalil.personalfinance.infrastructure.persistence.adapter;

import com.cauecalil.personalfinance.domain.model.Account;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import com.cauecalil.personalfinance.domain.repository.AccountRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.mapper.AccountMapper;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.AccountJpaRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.BankConnectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository accountJpaRepository;
    private final BankConnectionJpaRepository bankConnectionJpaRepository;

    @Override
    public void saveAll(List<Account> accounts) {
        List<AccountJpaEntity> entities = accounts.stream()
                .map(account -> {
                    BankConnectionJpaEntity bankConnectionRef = bankConnectionJpaRepository.getReferenceById(account.getBankConnectionId());
                    return AccountMapper.toEntity(account, bankConnectionRef);
                })
                .toList();

        accountJpaRepository.saveAll(entities);
    }

    @Override
    public Optional<Account> findById(String id) {
        return accountJpaRepository.findById(id).map(AccountMapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return accountJpaRepository.findAll().stream()
                .map(AccountMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumBalancesByType(AccountType type) {
        return accountJpaRepository.sumBalancesByType(type);
    }

    @Override
    public void deleteAll() {
        accountJpaRepository.deleteAll();
    }
}
