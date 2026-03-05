package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.exception.BankConnectionNotFoundException;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoveBankConnectionUseCase {
    private final BankConnectionRepository bankConnectionRepository;

    public void execute(Long id) {
        if (!bankConnectionRepository.existsById(id)) {
            throw new BankConnectionNotFoundException();
        }

        bankConnectionRepository.deleteById(id);
    }
}
