package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.GetUserCredentialStatusResponse;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserCredentialStatusUseCase {
    private final UserCredentialRepository userCredentialRepository;

    public GetUserCredentialStatusResponse execute() {
        UserCredential userCredential = userCredentialRepository.find().orElse(null);

        if (userCredential == null) {
            return GetUserCredentialStatusResponse.builder()
                    .configured(false)
                    .build();
        }

        return GetUserCredentialStatusResponse.builder()
                .configured(true)
                .build();
    }
}
