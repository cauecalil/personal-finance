package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.request.SaveUserCredentialRequest;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveUserCredentialUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final FinancialGateway financialGateway;

    public void execute(SaveUserCredentialRequest request) {
        UserCredential existing = userCredentialRepository.findFirst().orElse(null);

        if (existing != null) {
            financialGateway.invalidateCachedCredential(existing);
        }

        UserCredential credential = UserCredential.builder()
                .id(existing != null ? existing.getId() : null)
                .clientId(request.clientId())
                .clientSecret(request.clientSecret())
                .build();

        userCredentialRepository.save(credential);
    }
}
