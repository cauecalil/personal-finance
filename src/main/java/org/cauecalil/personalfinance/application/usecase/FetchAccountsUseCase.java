package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.internal.AccountData;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FetchAccountsUseCase {
    private final FinancialGateway financialGateway;

    public List<Account> execute(UserCredential userCredential, BankConnection bankConnection) {
        List<AccountData> accountDataList = financialGateway.fetchAccounts(userCredential, bankConnection);

        List<Account> accounts = new ArrayList<>();

        for (AccountData accountData : accountDataList) {
            Account account = Account.builder()
                    .id(accountData.id())
                    .bankConnectionId(bankConnection.getId())
                    .name(accountData.name())
                    .marketingName(accountData.marketingName())
                    .type(AccountType.valueOf(accountData.type()))
                    .subType(AccountSubType.valueOf(accountData.subType()))
                    .number(accountData.number())
                    .owner(accountData.owner())
                    .taxNumber(accountData.taxNumber())
                    .balance(accountData.balance())
                    .currency(accountData.currency())
                    .build();

            accounts.add(account);
        }

        return accounts;
    }
}
