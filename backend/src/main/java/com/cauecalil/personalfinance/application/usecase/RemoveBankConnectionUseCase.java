package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.exception.BankConnectionNotFoundException;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import lombok.RequiredArgsConstructor;
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
