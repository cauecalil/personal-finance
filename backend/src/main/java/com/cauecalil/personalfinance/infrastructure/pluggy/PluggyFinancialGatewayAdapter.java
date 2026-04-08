package com.cauecalil.personalfinance.infrastructure.pluggy;

import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.request.CreateConnectTokenRequest;
import ai.pluggy.client.request.TransactionsSearchRequest;
import ai.pluggy.client.response.*;
import com.cauecalil.personalfinance.application.port.FinancialGateway;
import com.cauecalil.personalfinance.domain.model.*;
import com.cauecalil.personalfinance.domain.model.Account;
import com.cauecalil.personalfinance.domain.model.Category;
import com.cauecalil.personalfinance.domain.model.Transaction;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import com.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import com.cauecalil.personalfinance.infrastructure.exception.PluggyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class PluggyFinancialGatewayAdapter implements FinancialGateway {
    private PluggyClient cachedClient;
    private String cachedClientId;
    private String cachedClientSecret;

    private PluggyClient buildClient(UserCredential credential) {
        if (cachedClient == null || !credential.getClientId().equals(cachedClientId) || !credential.getClientSecret().equals(cachedClientSecret)) {
            try {
                cachedClient = PluggyClient.builder()
                        .clientIdAndSecret(credential.getClientId(), credential.getClientSecret())
                        .build();

                cachedClientId = credential.getClientId();
                cachedClientSecret = credential.getClientSecret();
            } catch (Exception e) {
                throw new PluggyException("Invalid Pluggy credentials.", e);
            }
        }

        return cachedClient;
    }

    @Override
    public String createConnectionToken(UserCredential userCredential, String itemId) {
        log.debug("Requesting Pluggy Connect Token (updateMode: {})", itemId != null);

        PluggyClient client = buildClient(userCredential);

        try {
            CreateConnectTokenRequest request = CreateConnectTokenRequest.builder()
                    .itemId(itemId)
                    .build();

            Response<ConnectTokenResponse> response = client.service()
                    .createConnectToken(request)
                    .execute();

            if (!response.isSuccessful()) {
                throw new PluggyException("Failed to create connect token. HTTP %d".formatted(response.code()));
            }

            return Optional.ofNullable(response.body())
                    .map(ConnectTokenResponse::getAccessToken)
                    .orElseThrow(() -> new PluggyException("Failed to create connect token. No access token found."));
        } catch (Exception e) {
            throw new PluggyException("Network error creating connect token: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeConnection(UserCredential credential, String itemId) {
        log.debug("Removing connection for itemId: {}", itemId);

        PluggyClient client = buildClient(credential);

        try {
            Response<DeleteItemResponse> response = client.service()
                    .deleteItem(itemId)
                    .execute();

            if (!response.isSuccessful()) {
                throw new PluggyException("Failed to delete item. HTTP %d".formatted(response.code()));
            }

            Integer count = Optional.ofNullable(response.body())
                    .map(DeleteItemResponse::getCount)
                    .orElse(0);

            if (count == 0) {
                throw new PluggyException("Failed to delete item '%s'. No item found.".formatted(itemId));
            }
        } catch (IOException e) {
            throw new PluggyException("Network error deleting item '%s': %s".formatted(itemId, e.getMessage()), e);
        }
    }

    @Override
    public List<Category> fetchCategories(UserCredential userCredential) {
        log.debug("Fetching categories from Pluggy");

        PluggyClient client = buildClient(userCredential);

        try {
            Response<CategoriesResponse> response = client.service()
                    .getCategories()
                    .execute();

            if (!response.isSuccessful()) {
                throw new PluggyException("Failed to fetch categories. HTTP %d".formatted(response.code()));
            }

            return Optional.ofNullable(response.body())
                    .map(CategoriesResponse::getResults)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(category -> Category.builder()
                            .id(category.getId())
                            .description(category.getDescription())
                            //.descriptionTranslated(category.getDescriptionTranslated())
                            .parentId(category.getParentId())
                            .build())
                    .toList();
        } catch (IOException e) {
            throw new PluggyException("Network error fetching categories: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> fetchAccounts(UserCredential userCredential, BankConnection bankConnection) {
        log.debug("Fetching accounts for bank: {}", bankConnection.getBankName());

        PluggyClient client = buildClient(userCredential);

        try {
            Response<AccountsResponse> response = client.service()
                    .getAccounts(bankConnection.getItemId())
                    .execute();

            if (!response.isSuccessful()) {
                throw new PluggyException("Failed to fetch accounts for '%s'. HTTP %d".formatted(bankConnection.getBankName(), response.code()));
            }

            return Optional.ofNullable(response.body())
                    .map(AccountsResponse::getResults)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(account -> Account.builder()
                            .id(account.getId())
                            .bankConnectionId(bankConnection.getId())
                            .name(account.getName())
                            .marketingName(account.getMarketingName())
                            .type(AccountType.valueOf(account.getType()))
                            .subType(AccountSubType.valueOf(account.getSubtype()))
                            .number(account.getNumber())
                            .owner(account.getOwner())
                            .taxNumber(account.getTaxNumber())
                            .balance(BigDecimal.valueOf(account.getBalance()))
                            .currency(account.getCurrencyCode())
                            .build())
                    .toList();
        } catch (IOException e) {
            throw new PluggyException("Network error fetching accounts for '%s': %s".formatted(bankConnection.getBankName(), e.getMessage()), e);
        }
    }

    @Override
    public List<Transaction> fetchTransactions(UserCredential credential, String accountId) {
        log.debug("Fetching all transactions for account: {}", accountId);

        PluggyClient client = buildClient(credential);
        List<Transaction> allTransactions = new ArrayList<>();

        int currentPage = 1;
        int totalPages = 1;

        try {
            do {
                log.debug("Fetching page {} for account {}", currentPage, accountId);

                TransactionsSearchRequest searchRequest = new TransactionsSearchRequest()
                        .page(currentPage);

                Response<TransactionsResponse> response = client.service()
                        .getTransactions(accountId, searchRequest)
                        .execute();

                if (!response.isSuccessful()) {
                    throw new PluggyException("Failed to fetch transactions on page %d. HTTP %d".formatted(currentPage, response.code()));
                }

                TransactionsResponse body = response.body();

                if (body == null || body.getResults() == null || body.getResults().isEmpty()) {
                    log.warn("Empty body or results on page {} for account {}", currentPage, accountId);
                    break;
                }

                List<Transaction> pageResults = body.getResults().stream()
                        .map(transaction -> Transaction.builder()
                                .id(transaction.getId())
                                .accountId(accountId)
                                .description(transaction.getDescription())
                                .currency(transaction.getCurrencyCode())
                                .amount(BigDecimal.valueOf(transaction.getAmount()))
                                .amountInAccountCurrency(transaction.getAmountInAccountCurrency() == null ? null : BigDecimal.valueOf(transaction.getAmountInAccountCurrency()))
                                .type(TransactionType.valueOf(transaction.getType().name()))
                                .categoryId(transaction.getCategoryId())
                                .occurredAt(Instant.parse(transaction.getDate()))
                                .build())
                        .toList();

                allTransactions.addAll(pageResults);

                totalPages = body.getTotalPages() != null ? body.getTotalPages() : 1;

                currentPage++;
            } while (currentPage <= totalPages);

            log.debug("Successfully fetched {} transactions across {} pages for account {}", allTransactions.size(), totalPages, accountId);

            return allTransactions;
        } catch (IOException e) {
            throw new PluggyException("Network error fetching transactions: " + e.getMessage(), e);
        }
    }
}
