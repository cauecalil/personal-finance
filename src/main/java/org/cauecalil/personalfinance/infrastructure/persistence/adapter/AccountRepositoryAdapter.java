package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.AccountMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository accountJpaRepository;

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = AccountMapper.toEntity(account);
        AccountJpaEntity saved = accountJpaRepository.save(entity);
        return AccountMapper.toDomain(saved);
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
    public void delete(String id) {
        accountJpaRepository.deleteById(id);
    }
}
