package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListBankConnectionsUseCase {
    private final BankConnectionRepository bankConnectionRepository;

    public List<BankConnectionResponse> execute() {
        List<BankConnection> bankConnections = bankConnectionRepository.findAll();

        return bankConnections.stream().map(BankConnectionResponse::from).toList();
    }
}
