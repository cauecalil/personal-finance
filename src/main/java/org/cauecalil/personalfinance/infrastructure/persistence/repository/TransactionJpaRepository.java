package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, String> {
    Page<TransactionJpaEntity> findByAccountIdAndOccurredAtBetween(String accountId, Instant from, Instant to, Pageable pageable);
    Page<TransactionJpaEntity> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);
    void deleteByAccountId(String accountId);
}
