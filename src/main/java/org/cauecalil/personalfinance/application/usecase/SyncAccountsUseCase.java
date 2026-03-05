package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.internal.AccountData;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.Currency;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncAccountsUseCase {
    private final FinancialGateway financialGateway;
    private final AccountRepository accountRepository;

    public List<Account> execute(UserCredential userCredential, BankConnection bankConnection) {
        List<AccountData> accountDataList = financialGateway.fetchAccounts(userCredential, bankConnection);

        List<Account> accounts = new ArrayList<>();

        for (AccountData accountData : accountDataList) {
            Account account = accountRepository.findById(accountData.id())
                    .map(existingAccount -> {
                        existingAccount.updateBalance(accountData.balance());
                        return accountRepository.save(existingAccount);
                    })
                    .orElseGet(() -> {
                        Account newAccount = Account.builder()
                                .id(accountData.id())
                                .bankConnectionId(bankConnection.getId())
                                .name(accountData.name())
                                .type(accountData.type())
                                .subtype(accountData.subtype())
                                .balance(accountData.balance())
                                .currency(Currency.valueOf(accountData.currencyCode()))
                                .build();

                        return accountRepository.save(newAccount);
                    });

            accounts.add(account);
        }

        return accounts;
    }
}
