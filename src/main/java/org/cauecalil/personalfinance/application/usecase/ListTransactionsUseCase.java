package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.exception.ListTransactionsFromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTransactionsUseCase {
    private final TransactionRepository transactionRepository;
    private final ZoneId zoneId;

    public List<TransactionResponse> execute(String accountId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ListTransactionsFromDateAfterToDateException();
        }

        Instant startInstant = from.atStartOfDay(zoneId).toInstant();
        Instant endInstant = to.atTime(LocalTime.MAX).atZone(zoneId).toInstant();

        List<Transaction> transactions = transactionRepository.findByAccountIdAndOccurredAtBetween(accountId, startInstant, endInstant);

        return transactions
                .stream()
                .map(transaction -> TransactionResponse.from(transaction, zoneId))
                .toList();
    }
}
