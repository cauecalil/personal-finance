package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, String> {
    List<TransactionJpaEntity> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to);
    void deleteByAccountId(String accountId);
}
