package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Schema(description = "Category aggregations for expenses and income within the selected period.")
public record GetDashboardCategoriesResponse(
    @ArraySchema(arraySchema = @Schema(description = "Expense totals grouped by category."), schema = @Schema(implementation = CategoryResponse.class))
    List<CategoryResponse> expenses,

    @ArraySchema(arraySchema = @Schema(description = "Income totals grouped by category."), schema = @Schema(implementation = CategoryResponse.class))
    List<CategoryResponse> income
) {
    @Builder
    @Schema(description = "Total amount aggregated for a single category.")
    public record CategoryResponse(
            @Schema(description = "Business category label used for grouping transactions.", example = "Groceries", requiredMode = Schema.RequiredMode.REQUIRED)
            String category,

            @Schema(description = "Absolute total amount aggregated for the category.", example = "742.19", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal total
    ) {}
}
