package com.cauecalil.personalfinance.infrastructure.persistence.repository;

import com.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, String> {
}
