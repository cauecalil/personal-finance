package com.cauecalil.personalfinance.infrastructure.persistence.adapter;

import com.cauecalil.personalfinance.domain.model.Category;
import com.cauecalil.personalfinance.domain.repository.CategoryRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.mapper.CategoryMapper;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
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
