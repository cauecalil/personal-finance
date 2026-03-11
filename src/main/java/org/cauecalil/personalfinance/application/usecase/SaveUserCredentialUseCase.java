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
        UserCredential credential = UserCredential.builder()
                .clientId(request.clientId())
                .clientSecret(request.clientSecret())
                .build();

        userCredentialRepository.save(credential);
    }
}
