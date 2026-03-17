package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.repository.CategoryRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.CategoryMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public List<Category> saveAll(List<Category> categories) {
        List<CategoryJpaEntity> entities = categories.stream()
                .map(CategoryMapper::toEntity)
                .toList();

        return categoryJpaRepository.saveAll(entities)
                .stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllById(Set<String> ids) {
        return categoryJpaRepository.findAllById(ids)
                .stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }
}
