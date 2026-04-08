package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.request.SaveUserCredentialRequest;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
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
