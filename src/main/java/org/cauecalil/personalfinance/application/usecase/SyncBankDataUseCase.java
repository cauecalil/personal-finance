package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.response.SyncBankDataResponse;
import org.cauecalil.personalfinance.application.exception.BankConnectionNotFoundException;
import org.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncBankDataUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final BankConnectionRepository bankConnectionRepository;
    private final SyncAccountsUseCase syncAccountsUseCase;
    private final SyncTransactionsUseCase syncTransactionsUseCase;

    public SyncBankDataResponse execute() {
        UserCredential userCredential = userCredentialRepository.find()
                .orElseThrow(UserCredentialNotFoundException::new);

        List<BankConnection> bankConnections = bankConnectionRepository.findAll();

        if (bankConnections.isEmpty()) {
            throw new BankConnectionNotFoundException();
        }

        log.info("Starting sync for {} bank connection(s)", bankConnections.size());

        int accountsSynced  = 0;
        int transactionsSynced  = 0;

        for (BankConnection bankConnection : bankConnections) {
            log.info("Syncing bank: {}", bankConnection.getBankName());

            try {
                List<Account> accounts = syncAccountsUseCase.execute(userCredential, bankConnection);
                accountsSynced += accounts.size();
                transactionsSynced += syncTransactionsUseCase.execute(userCredential, accounts);
                bankConnection.markSynced(BankConnectionStatus.UPDATED);
            } catch (Exception e) {
                log.error("Failed to sync bank {}: {}", bankConnection.getBankName(), e.getMessage());
                bankConnection.markSynced(BankConnectionStatus.ERROR);
            }

            bankConnectionRepository.save(bankConnection);
        }

        return SyncBankDataResponse.builder()
                .accountsSynced(accountsSynced)
                .transactionsSynced(transactionsSynced)
                .build();
    }
}
