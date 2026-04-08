package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GetDashboardMetricsResponse;
import com.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import com.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import com.cauecalil.personalfinance.domain.model.Account;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import com.cauecalil.personalfinance.domain.repository.AccountRepository;
import com.cauecalil.personalfinance.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetDashboardMetricsUseCase {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetDashboardMetricsResponse execute(String accountId, Instant fromInstant, Instant toInstant) {
        if (fromInstant.isAfter(toInstant)) {
            throw new FromDateAfterToDateException();
        }

        Optional<Account> account = Optional.ofNullable(accountId).flatMap(accountRepository::findById);

        if (accountId != null && account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        BigDecimal currentBalance = account
                .map(Account::getBalance)
                .orElseGet(() -> accountRepository.sumBalancesByType(AccountType.BANK));

        TransactionRepository.Metrics metrics = transactionRepository.findMetrics(accountId, fromInstant, toInstant);

        String currencyCode = account.isPresent() ? account.get().getCurrency() : "BRL";

        return GetDashboardMetricsResponse.builder()
                .currentBalance(currentBalance)
                .totalIncome(metrics.totalIncome())
                .totalExpenses(metrics.totalExpenses())
                .currencyCode(currencyCode)
                .build();
    }
}
