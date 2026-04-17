package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Summary counters for the latest bank data synchronization run.")
public record SyncBankDataResponse(
        @Schema(description = "Number of categories retrieved and persisted during synchronization.", example = "122", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer categoriesSynced,

        @Schema(description = "Number of accounts synchronized across all configured bank connections.", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer accountsSynced,

        @Schema(description = "Number of transactions synchronized and classified.", example = "842", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer transactionsSynced
) {}
