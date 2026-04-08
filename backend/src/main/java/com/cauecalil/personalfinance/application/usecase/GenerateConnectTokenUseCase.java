package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GenerateConnectTokenResponse;
import com.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import com.cauecalil.personalfinance.application.port.FinancialGateway;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateConnectTokenUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final FinancialGateway financialGateway;

    public GenerateConnectTokenResponse execute(String itemId) {
        UserCredential userCredential = userCredentialRepository.find()
                .orElseThrow(UserCredentialNotFoundException::new);

        String connectToken = financialGateway.createConnectionToken(userCredential, itemId);

        return GenerateConnectTokenResponse.builder()
                .connectToken(connectToken)
                .build();
    }
}
