package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, String> {
}
