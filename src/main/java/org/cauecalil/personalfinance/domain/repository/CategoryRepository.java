package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Category;

import java.util.List;

public interface CategoryRepository {
    void saveAll(List<Category> categories);
}
