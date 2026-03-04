package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.UserCredentialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialJpaEntity, Long> {
}
