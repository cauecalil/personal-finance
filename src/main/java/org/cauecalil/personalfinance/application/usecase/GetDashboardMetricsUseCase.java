package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.GetDashboardMetricsResponse;
import org.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import org.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
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
