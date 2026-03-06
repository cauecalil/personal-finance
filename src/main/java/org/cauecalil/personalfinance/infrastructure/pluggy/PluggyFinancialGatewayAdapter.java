package org.cauecalil.personalfinance.infrastructure.pluggy;

import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.response.Account;
import ai.pluggy.client.response.AccountsResponse;
import ai.pluggy.client.response.Transaction;
import ai.pluggy.client.response.TransactionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.internal.AccountData;
import org.cauecalil.personalfinance.application.dto.internal.TransactionData;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.cauecalil.personalfinance.infrastructure.exception.PluggyAuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class PluggyFinancialGatewayAdapter implements FinancialGateway {
    @Override
    public String createConnectionToken(UserCredential userCredential, String itemId) {
        log.debug("Requesting Pluggy Connect Token (updateMode: {})", itemId != null);

        try {
            return createConnectTokenManual(userCredential, itemId);
        } catch (Exception e) {
            throw new PluggyAuthException("Error requesting connect token: " + e.getMessage(), e);
        }
    }

    // this workaround is required because the pluggy jdk has not yet been updated to support the avoidDuplicates parameter
    @SuppressWarnings("unchecked")
    private String createConnectTokenManual(UserCredential credential, String itemId) {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.pluggy.ai")
                .build();

        Map<String, Object> authResponse = restClient.post()
                .uri("/auth")
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "clientId", credential.getClientId(),
                        "clientSecret", credential.getClientSecret()
                ))
                .retrieve()
                .body(Map.class);

        if (authResponse == null || !authResponse.containsKey("apiKey")) {
            throw new PluggyAuthException("Pluggy authentication failed. Verify your Client ID and Client Secret.");
        }

        String apiKey = (String) authResponse.get("apiKey");

        boolean isNewConnection = itemId == null || itemId.isBlank();

        Map<String, Object> body = new HashMap<>();
        if (!isNewConnection) {
            body.put("itemId", itemId);
        }

        body.put("options", Map.of("avoidDuplicates", isNewConnection));

        Map<String, Object> tokenResponse = restClient.post()
                .uri("/connect_token")
                .header("Content-Type", "application/json")
                .header("X-API-KEY", apiKey)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (tokenResponse == null || !tokenResponse.containsKey("accessToken")) {
            throw new PluggyAuthException("Pluggy did not return a connect token. Please try again.");
        }

        return (String) tokenResponse.get("accessToken");
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

    private PluggyClient buildClient(UserCredential credential) {
        try {
            return PluggyClient.builder()
                    .clientIdAndSecret(credential.getClientId(), credential.getClientSecret())
                    .build();
        } catch (Exception e) {
            throw new PluggyAuthException("Invalid Pluggy credentials. Please check your Client ID and Client Secret.", e);
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
