package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.TransactionMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = TransactionMapper.toEntity(transaction);
        TransactionJpaEntity saved = transactionJpaRepository.save(entity);
        return TransactionMapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return transactionJpaRepository.findById(id).map(TransactionMapper::toDomain);
    }

    @Override
    public void delete(String id) {
        transactionJpaRepository.deleteById(id);
    }
}
