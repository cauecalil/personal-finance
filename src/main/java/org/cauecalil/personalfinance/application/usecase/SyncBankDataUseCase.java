package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.response.SyncBankDataResponse;
import org.cauecalil.personalfinance.application.exception.BankConnectionNotFoundException;
import org.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncBankDataUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final SyncCategoriesUseCase syncCategoriesUseCase;
    private final BankConnectionRepository bankConnectionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialGateway financialGateway;

    public SyncBankDataResponse execute() {
        UserCredential userCredential = userCredentialRepository.find()
                .orElseThrow(UserCredentialNotFoundException::new);

        int categoriesSynced = syncCategoriesUseCase.execute(userCredential);

        List<BankConnection> bankConnections = bankConnectionRepository.findAll();

        if (bankConnections.isEmpty()) {
            throw new BankConnectionNotFoundException();
        }

        log.info("Starting sync for {} bank connection(s)", bankConnections.size());

        int accountsSynced  = 0;
        int transactionsSynced  = 0;

        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        for (BankConnection bankConnection : bankConnections) {
            log.info("Syncing bank: {}", bankConnection.getBankName());

            try {
                List<Account> accounts = financialGateway.fetchAccounts(userCredential, bankConnection);

                List<Transaction> transactions = accounts.stream()
                        .flatMap(account -> financialGateway.fetchTransactions(userCredential, account.getId()).stream())
                        .collect(Collectors.toList());

                accountRepository.saveAll(accounts);
                transactionRepository.saveAll(transactions);

                accountsSynced += accounts.size();
                transactionsSynced += transactions.size();

                bankConnection.markSynced(BankConnectionStatus.UPDATED);
            } catch (Exception e) {
                log.error("Failed to sync bank {}: {}", bankConnection.getBankName(), e.getMessage());
                bankConnection.markSynced(BankConnectionStatus.ERROR);
            }

            bankConnectionRepository.save(bankConnection);
        }

        return SyncBankDataResponse.builder()
                .categoriesSynced(categoriesSynced)
                .accountsSynced(accountsSynced)
                .transactionsSynced(transactionsSynced)
                .build();
    }
}
