package org.cauecalil.personalfinance.infrastructure.persistence.repository;

import org.cauecalil.personalfinance.infrastructure.persistence.entity.UserCredentialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialJpaEntity, Long> {
    Optional<UserCredentialJpaEntity> findFirstByOrderByIdAsc();
}
