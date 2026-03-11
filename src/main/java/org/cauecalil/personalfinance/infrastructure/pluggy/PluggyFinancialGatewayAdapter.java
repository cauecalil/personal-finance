package org.cauecalil.personalfinance.infrastructure.pluggy;

import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.request.CreateConnectTokenRequest;
import ai.pluggy.client.response.*;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.internal.AccountData;
import org.cauecalil.personalfinance.application.dto.internal.TransactionData;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.cauecalil.personalfinance.infrastructure.exception.PluggyAuthException;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
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
                throw new PluggyAuthException("Invalid Pluggy credentials.", e);
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
                throw new PluggyAuthException("Failed to create connect token. HTTP %d".formatted(response.code()));
            }

            return Optional.ofNullable(response.body())
                    .map(ConnectTokenResponse::getAccessToken)
                    .orElseThrow(() -> new PluggyAuthException("Failed to create connect token. No access token found."));
        } catch (Exception e) {
            throw new PluggyAuthException("Network error creating connect token: " + e.getMessage(), e);
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
                throw new PluggyAuthException("Failed to delete item. HTTP %d".formatted(response.code()));
            }

            Integer count = Optional.ofNullable(response.body())
                    .map(DeleteItemResponse::getCount)
                    .orElse(0);

            if (count == 0) {
                throw new PluggyAuthException("Failed to delete item '%s'. No item found.".formatted(itemId));
            }
        } catch (IOException e) {
            throw new PluggyAuthException("Network error deleting item '%s': %s".formatted(itemId, e.getMessage()), e);
        }
    }

    @Override
    public List<AccountData> fetchAccounts(UserCredential userCredential, BankConnection bankConnection) {
        log.debug("Fetching accounts for bank: {}", bankConnection.getBankName());

        PluggyClient client = buildClient(userCredential);

        try {
            Response<AccountsResponse> response = client.service()
                    .getAccounts(bankConnection.getItemId())
                    .execute();

            if (!response.isSuccessful()) {
                throw new PluggyAuthException("Failed to fetch accounts for '%s'. HTTP %d".formatted(bankConnection.getBankName(), response.code()));
            }

            return Optional.ofNullable(response.body())
                    .map(AccountsResponse::getResults)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(this::toAccountData)
                    .toList();
        } catch (IOException e) {
            throw new PluggyAuthException("Network error fetching accounts for '%s': %s".formatted(bankConnection.getBankName(), e.getMessage()), e);
        }
    }

    @Override
    public List<TransactionData> fetchTransactions(UserCredential credential, String accountId) {
        log.debug("Fetching transactions for account: {}", accountId);

        PluggyClient client = buildClient(credential);

        try {
            Response<TransactionsResponse> response = client.service()
                    .getTransactions(accountId)
                    .execute();

            if (!response.isSuccessful()) {
                throw new PluggyAuthException("Failed to fetch transactions. HTTP %d".formatted(response.code()));
            }

            return Optional.ofNullable(response.body())
                    .map(TransactionsResponse::getResults)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(this::toTransactionData)
                    .toList();
        } catch (IOException e) {
            throw new PluggyAuthException("Network error fetching transactions: " + e.getMessage(), e);
        }
    }

    private AccountData toAccountData(Account account) {
        return AccountData.builder()
                .id(account.getId())
                .name(account.getName())
                .marketingName(account.getMarketingName())
                .type(account.getType())
                .subType(account.getSubtype())
                .number(account.getNumber())
                .owner(account.getOwner())
                .taxNumber(account.getTaxNumber())
                .balance(BigDecimal.valueOf(account.getBalance()))
                .currency(account.getCurrencyCode())
                .build();
    }

    private TransactionData toTransactionData(Transaction transaction) {
        var amountInAccountCurrency = transaction.getAmountInAccountCurrency() == null ? null : BigDecimal.valueOf(transaction.getAmountInAccountCurrency());

        return TransactionData.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .currency(transaction.getCurrencyCode())
                .amount(BigDecimal.valueOf(transaction.getAmount()))
                .amountInAccountCurrency(amountInAccountCurrency)
                .type(TransactionType.from(transaction.getType().name(), BigDecimal.valueOf(transaction.getAmount())))
                .category(transaction.getCategory())
                .occurredAt(Instant.parse(transaction.getDate()))
                .build();
    }
}
