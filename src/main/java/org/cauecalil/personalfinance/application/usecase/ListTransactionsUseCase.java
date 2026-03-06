package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.exception.ListTransactionsFromDateAfterToDateException;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public List<TransactionResponse> execute(String accountId, Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new ListTransactionsFromDateAfterToDateException();
        }

        List<Transaction> transactions = transactionRepository.findByAccountIdAndOccurredAtBetween(accountId, from, to);

        return transactions
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
