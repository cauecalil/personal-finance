package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

@Builder
public record SyncBankDataResponse(
        Integer categoriesSynced,
        Integer accountsSynced,
        Integer transactionsSynced
) {}
