package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;

public class CategoryMapper {
    public static CategoryJpaEntity toEntity(Category category) {
        return CategoryJpaEntity.builder()
                .id(category.getId())
                .description(category.getDescription())
                .descriptionTranslated(category.getDescriptionTranslated())
                .parentId(category.getParentId())
                .build();
    }

    public static Category toDomain(CategoryJpaEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .descriptionTranslated(entity.getDescriptionTranslated())
                .parentId(entity.getParentId())
                .build();
    }
}
