package com.cauecalil.personalfinance.application.port;

import com.cauecalil.personalfinance.domain.model.*;

import java.util.List;

public interface FinancialGateway {
    String createConnectionToken(UserCredential userCredential, String itemId);
    void removeConnection(UserCredential credential, String itemId);
    List<Category> fetchCategories(UserCredential userCredential);
    List<Account> fetchAccounts(UserCredential userCredential, BankConnection bankConnection);
    List<Transaction> fetchTransactions(UserCredential userCredential, String accountId);
}
