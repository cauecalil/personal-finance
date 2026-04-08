package com.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record GetDashboardCategoriesResponse(
    List<CategoryResponse> expenses,
    List<CategoryResponse> income
) {
    @Builder
    public record CategoryResponse(
            String category,
            BigDecimal total
    ) {}
}
