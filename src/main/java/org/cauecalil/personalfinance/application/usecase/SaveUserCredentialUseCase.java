package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.request.SaveUserCredentialRequest;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveUserCredentialUseCase {
    private final UserCredentialRepository userCredentialRepository;

    public void execute(SaveUserCredentialRequest request) {
        Long existingId = userCredentialRepository.findFirst()
                .map(UserCredential::getId)
                .orElse(null);

        UserCredential credential = UserCredential.builder()
                .id(existingId)
                .clientId(request.clientId().toString())
                .clientSecret(request.clientSecret().toString())
                .build();

        userCredentialRepository.save(credential);
    }
}
