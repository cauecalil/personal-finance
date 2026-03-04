package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankConnectionJpaRepository extends JpaRepository<BankConnectionJpaEntity, Long> {
}
