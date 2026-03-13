package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.TransactionMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.AccountJpaRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository transactionJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Override
    public void saveAll(List<Transaction> transactions) {
        List<TransactionJpaEntity> entities = transactions.stream()
                .map(transaction -> {
                    AccountJpaEntity accountRef = accountJpaRepository.getReferenceById(transaction.getAccountId());
                    return TransactionMapper.toEntity(transaction, accountRef);
                })
                .toList();

        transactionJpaRepository.saveAll(entities);
    }

    @Override
    public Page<Transaction> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable) {
        Page<TransactionJpaEntity> page = transactionJpaRepository.findByAccountIdAndOccurredAtBetween(accountId, from, to, pageable);
        return page.map(TransactionMapper::toDomain);
    }

    @Override
    public Page<Transaction> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable) {
        return transactionJpaRepository.findByOccurredAtBetween(from, to, pageable)
                .map(TransactionMapper::toDomain);
    }

    @Override
    public boolean existsById(String id) {
        return transactionJpaRepository.existsById(id);
    }

    @Override
    public void deleteAll() {
        transactionJpaRepository.deleteAll();
    }
}
