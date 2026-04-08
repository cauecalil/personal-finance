package com.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ListTransactionsResponse(
        List<TransactionResponse> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages,
        boolean hasNextPage,
        boolean hasPreviousPage
) {}
