package org.cauecalil.personalfinance.application.port;

import org.cauecalil.personalfinance.application.dto.internal.AccountData;
import org.cauecalil.personalfinance.application.dto.internal.TransactionData;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;

import java.util.List;

public interface FinancialGateway {
    String createConnectionToken(UserCredential userCredential, String itemId);
    void removeConnection(UserCredential credential, String itemId);
    List<AccountData> fetchAccounts(UserCredential userCredential, BankConnection bankConnection);
    List<TransactionData> fetchTransactions(UserCredential userCredential, String accountId);
}
