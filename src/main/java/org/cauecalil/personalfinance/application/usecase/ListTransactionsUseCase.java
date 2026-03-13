package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.ListTransactionsResponse;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListTransactionsUseCase {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ListTransactionsResponse execute(String accountId, Instant fromInstant, Instant toInstant, int page, int pageSize, Sort.Direction sort) {
        if (fromInstant.isAfter(toInstant)) {
            throw new FromDateAfterToDateException();
        }

        Optional<Account> account = Optional.ofNullable(accountId).flatMap(accountRepository::findById);

        if (accountId != null && account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        Sort sortBy = Sort.by(sort, "occurredAt");
        Pageable pageable = PageRequest.of(page, pageSize, sortBy);

        Page<Transaction> transactions = account
                .map(acc -> transactionRepository.findByAccountIdAndOccurredAtBetween(acc.getId(), fromInstant, toInstant, pageable))
                .orElseGet(() -> transactionRepository.findByOccurredAtBetween(fromInstant, toInstant, pageable));

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
