package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.GetDashboardCategoriesResponse;
import org.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetDashboardCategoriesUseCase {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetDashboardCategoriesResponse execute(String accountId, Instant fromInstant, Instant toInstant) {
        if (fromInstant.isAfter(toInstant)) {
            throw new FromDateAfterToDateException();
        }

        Optional<Account> account = Optional.ofNullable(accountId).flatMap(accountRepository::findById);

        if (accountId != null && account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        List<TransactionRepository.CategoryAggregation> aggregations = transactionRepository
                .findCategoryAggregations(accountId, fromInstant, toInstant);

        List<GetDashboardCategoriesResponse.CategoryResponse> expenses = aggregations.stream()
                .filter(aggregation -> aggregation.type() == TransactionType.DEBIT)
                .map(aggregation -> GetDashboardCategoriesResponse.CategoryResponse.builder()
                        .category(aggregation.category())
                        .total(aggregation.total())
                        .build())
                .toList();

        List<GetDashboardCategoriesResponse.CategoryResponse> income = aggregations.stream()
                .filter(aggregation -> aggregation.type() == TransactionType.CREDIT)
                .map(aggregation -> GetDashboardCategoriesResponse.CategoryResponse.builder()
                        .category(aggregation.category())
                        .total(aggregation.total())
                        .build())
                .toList();

        return GetDashboardCategoriesResponse.builder()
                .expenses(expenses)
                .income(income)
                .build();
    }
}
