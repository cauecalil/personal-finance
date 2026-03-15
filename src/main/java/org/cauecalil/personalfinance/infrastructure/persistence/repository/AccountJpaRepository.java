package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, String> {
	@Query("""
		SELECT SUM(balance)
		FROM AccountJpaEntity
		WHERE type = :type
	""")
	BigDecimal sumBalancesByType(AccountType type);
}
