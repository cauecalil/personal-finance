package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.internal.TransactionData;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FetchTransactionsUseCase {
    private final FinancialGateway financialGateway;

    public List<Transaction> execute(UserCredential userCredential, List<Account> accounts) {
        List<Transaction> transactions = new ArrayList<>();

        for (Account account : accounts) {
            List<TransactionData> transactionDataList = financialGateway.fetchTransactions(userCredential, account.getId());

            for (TransactionData transactionData : transactionDataList) {
                Transaction transaction = Transaction.builder()
                        .id(transactionData.id())
                        .accountId(account.getId())
                        .description(transactionData.description())
                        .currency(transactionData.currency())
                        .amount(transactionData.amount())
                        .amountInAccountCurrency(transactionData.amountInAccountCurrency())
                        .type(transactionData.type())
                        .category(transactionData.category())
                        .occurredAt(transactionData.occurredAt())
                        .build();

                transactions.add(transaction);
            }
        }

        return transactions;
    }
}
