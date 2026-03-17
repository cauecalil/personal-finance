package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncCategoriesUseCase {
    private final FinancialGateway financialGateway;
    private final CategoryRepository categoryRepository;

    public List<Category> execute(UserCredential userCredential) {
        List<Category> categories = financialGateway.fetchCategories(userCredential);
        List<Category> categoriesWithRoot = assignRootCategoryIds(categories);
        return categoryRepository.saveAll(categoriesWithRoot);
    }

    private List<Category> assignRootCategoryIds(List<Category> categories) {
        Map<String, Category> categoriesById = categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        Map<String, String> rootByCategoryId = new HashMap<>();

        return categories.stream()
                .map(category -> Category.builder()
                        .id(category.getId())
                        .description(category.getDescription())
                        .descriptionTranslated(category.getDescriptionTranslated())
                        .parentId(category.getParentId())
                        .rootCategoryId(resolveRootCategoryId(category.getId(), categoriesById, rootByCategoryId, new HashSet<>()))
                        .build())
                .toList();
    }

    private String resolveRootCategoryId(
            String categoryId,
            Map<String, Category> categoriesById,
            Map<String, String> rootByCategoryId,
            Set<String> visiting
    ) {
        if (categoryId == null) {
            return null;
        }

        if (rootByCategoryId.containsKey(categoryId)) {
            return rootByCategoryId.get(categoryId);
        }

        if (!visiting.add(categoryId)) {
            // Defensive fallback for malformed external hierarchies with cycles.
            rootByCategoryId.put(categoryId, categoryId);
            return categoryId;
        }

        try {
            Category category = categoriesById.get(categoryId);
            if (category == null) {
                rootByCategoryId.put(categoryId, categoryId);
                return categoryId;
            }

            String parentId = category.getParentId();
            String resolvedRoot = (parentId == null || parentId.isBlank() || !categoriesById.containsKey(parentId))
                    ? categoryId
                    : resolveRootCategoryId(parentId, categoriesById, rootByCategoryId, visiting);

            rootByCategoryId.put(categoryId, resolvedRoot);
            return resolvedRoot;
        } finally {
            visiting.remove(categoryId);
        }
    }
}


