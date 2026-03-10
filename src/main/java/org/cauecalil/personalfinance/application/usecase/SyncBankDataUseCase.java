package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.response.SyncBankDataResponse;
import org.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncBankDataUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final BankConnectionRepository bankConnectionRepository;
    private final SyncAccountsUseCase syncAccountsUseCase;
    private final SyncTransactionsUseCase syncTransactionsUseCase;

    public SyncBankDataResponse execute(boolean fullSync) {
        UserCredential userCredential = userCredentialRepository.findFirst()
                .orElseThrow(UserCredentialNotFoundException::new);

        List<BankConnection> bankConnections = bankConnectionRepository.findAll();

        if (bankConnections.isEmpty()) {
            return SyncBankDataResponse.builder()
                    .accountsSynced(0)
                    .transactionsSynced(0)
                    .message("No bank connections found. Please add a bank first.")
                    .build();
        }

        log.info("Starting sync for {} bank connection(s). Full sync: {}", bankConnections.size(), fullSync);

        int accountsSynced  = 0;
        int transactionsSynced  = 0;
        List<String> errors = new ArrayList<>();

        for (BankConnection bankConnection : bankConnections) {
            log.info("Syncing bank: {}", bankConnection.getBankName());

            try {
                List<Account> accounts = syncAccountsUseCase.execute(userCredential, bankConnection);
                accountsSynced += accounts.size();
                transactionsSynced += syncTransactionsUseCase.execute(userCredential, accounts, fullSync);
                bankConnection.markSynced(BankConnectionStatus.UPDATED);
            } catch (Exception e) {
                log.error("Failed to sync bank {}: {}", bankConnection.getBankName(), e.getMessage());
                bankConnection.markSynced(BankConnectionStatus.ERROR);
                errors.add(bankConnection.getBankName());
            }

            bankConnectionRepository.save(bankConnection);
        }

        String message = errors.isEmpty()
                ? "Sync completed: %d accounts and %d transactions updated.".formatted(accountsSynced, transactionsSynced)
                : "Sync completed with errors in: %s".formatted(String.join(", ", errors));

        return SyncBankDataResponse.builder()
                .accountsSynced(accountsSynced)
                .transactionsSynced(transactionsSynced)
                .message(message)
                .build();
    }
}
