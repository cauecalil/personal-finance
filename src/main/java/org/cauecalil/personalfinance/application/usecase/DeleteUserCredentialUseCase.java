package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserCredentialUseCase {
    private final UserCredentialRepository userCredentialRepository;

    public void execute() {
        userCredentialRepository.findFirst().ifPresent(
                userCredential -> userCredentialRepository.delete(userCredential.getId())
        );
    }
}
