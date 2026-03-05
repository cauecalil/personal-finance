package org.cauecalil.personalfinance.infrastructure.pluggy;

import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.infrastructure.exception.PluggyAuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

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
}
