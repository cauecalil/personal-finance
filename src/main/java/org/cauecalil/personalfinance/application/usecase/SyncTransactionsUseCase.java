package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.internal.TransactionData;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncTransactionsUseCase {
    private final FinancialGateway financialGateway;
    private final TransactionRepository transactionRepository;

    public int execute(UserCredential userCredential, List<Account> accounts, boolean fullSync) {
        int transactionsSynced = 0;

        for (Account account : accounts) {
            if (fullSync) {
                transactionRepository.deleteByAccountId(account.getId());
            }

            List<TransactionData> transactionDataList = financialGateway.fetchTransactions(userCredential, account.getId());

            for (TransactionData transactionData : transactionDataList) {
                if (transactionRepository.existsById(transactionData.id())) {
                    continue;
                }

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

                transactionRepository.save(transaction);

                transactionsSynced++;
            }
        }

        return transactionsSynced;
    }
}
