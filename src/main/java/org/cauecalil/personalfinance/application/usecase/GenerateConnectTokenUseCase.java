package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.GenerateConnectTokenResponse;
import org.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenerateConnectTokenUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final FinancialGateway financialGateway;

    public GenerateConnectTokenResponse execute(String itemId) {
        Optional<UserCredential> userCredential = userCredentialRepository.findFirst();

        if (userCredential.isEmpty()) {
            throw new UserCredentialNotFoundException();
        }

        String connectToken = financialGateway.createConnectionToken(userCredential.get(), itemId);

        return GenerateConnectTokenResponse.builder()
                .connectToken(connectToken)
                .build();
    }
}
