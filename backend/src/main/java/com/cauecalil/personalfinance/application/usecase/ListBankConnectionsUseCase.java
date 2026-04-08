package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListBankConnectionsUseCase {
    private final BankConnectionRepository bankConnectionRepository;

    public List<BankConnectionResponse> execute() {
        List<BankConnection> bankConnections = bankConnectionRepository.findAll();

        return bankConnections
                .stream()
                .map(BankConnectionResponse::from)
                .toList();
    }
}
