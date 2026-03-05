package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, String> {
    void deleteByAccountId(String accountId);
}
