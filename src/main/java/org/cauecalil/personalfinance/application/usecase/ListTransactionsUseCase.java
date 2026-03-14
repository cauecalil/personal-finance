package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.ListTransactionsResponse;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.CategoryRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListTransactionsUseCase {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

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

        Set<String> categoryIds = transactions
                .getContent()
                .stream()
                .map(Transaction::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Category> categoriesMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        List<TransactionResponse> transactionsDto = transactions
                .getContent()
                .stream()
                .map(transaction -> {
                    Category category = categoriesMap.get(transaction.getCategoryId());
                    return TransactionResponse.from(transaction, category);
                })
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
