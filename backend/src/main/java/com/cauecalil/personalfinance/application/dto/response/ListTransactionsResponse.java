package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Paginated transaction listing with navigation metadata.")
public record ListTransactionsResponse(
        @ArraySchema(arraySchema = @Schema(description = "Transactions for the requested page."), schema = @Schema(implementation = TransactionResponse.class))
        List<TransactionResponse> items,

        @Schema(description = "Zero-based page index returned by the query.", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int page,

        @Schema(description = "Maximum number of transactions per page.", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
        int pageSize,

        @Schema(description = "Total number of transactions matching the current filters.", example = "183", requiredMode = Schema.RequiredMode.REQUIRED)
        long totalItems,

        @Schema(description = "Total number of pages available for the current filters.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        long totalPages,

        @Schema(description = "True when another page exists after this one.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasNextPage,

        @Schema(description = "True when a previous page exists before this one.", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasPreviousPage
) {}
