package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.repository.CategoryRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.CategoryMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public void saveAll(List<Category> categories) {
        List<CategoryJpaEntity> entities = categories.stream()
                .map(CategoryMapper::toEntity)
                .toList();

        categoryJpaRepository.saveAll(entities);
    }
}
