package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GetUserCredentialStatusResponse;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
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
