package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.Category;

import java.util.List;
import java.util.Set;

public interface CategoryRepository {
    List<Category> saveAll(List<Category> categories);
    List<Category> findAllById(Set<String> ids);
}
