package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.ListTransactionsResponse;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public ListTransactionsResponse execute(String accountId, Instant from, Instant to, int page, int pageSize) {
        if (from.isAfter(to)) {
            throw new FromDateAfterToDateException();
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Transaction> transactions = transactionRepository.findByAccountIdAndOccurredAtBetween(accountId, from, to, pageable);

        List<TransactionResponse> transactionsDto = transactions
                .getContent()
                .stream()
                .map(TransactionResponse::from)
                .toList();

        return ListTransactionsResponse.builder()
                .items(transactionsDto)
                .page(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalItems(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .hasNextPage(transactions.hasNext())
                .hasPreviousPage(transactions.hasPrevious())
                .build();
    }
}
