package com.cauecalil.personalfinance.infrastructure.persistence.mapper;

import com.cauecalil.personalfinance.domain.model.Category;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;

public class CategoryMapper {
    public static CategoryJpaEntity toEntity(Category category) {
        return CategoryJpaEntity.builder()
                .id(category.getId())
                .description(category.getDescription())
                .descriptionTranslated(category.getDescriptionTranslated())
                .parentId(category.getParentId())
                .rootCategoryId(category.getRootCategoryId())
                .build();
    }

    public static Category toDomain(CategoryJpaEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .descriptionTranslated(entity.getDescriptionTranslated())
                .parentId(entity.getParentId())
                .rootCategoryId(entity.getRootCategoryId())
                .build();
    }
}
