package com.cauecalil.personalfinance.infrastructure.persistence.repository;

import com.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankConnectionJpaRepository extends JpaRepository<BankConnectionJpaEntity, Long> {
    boolean existsByItemId(String itemId);
}
