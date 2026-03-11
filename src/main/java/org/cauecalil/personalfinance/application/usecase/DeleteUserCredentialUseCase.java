package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import org.cauecalil.personalfinance.application.port.FinancialGateway;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUserCredentialUseCase {
    private final UserCredentialRepository userCredentialRepository;
    private final BankConnectionRepository bankConnectionRepository;
    private final FinancialGateway financialGateway;

    public void execute() {
        UserCredential userCredential = userCredentialRepository.find().orElseThrow(UserCredentialNotFoundException::new);

        List<BankConnection> bankConnections = bankConnectionRepository.findAll();

        bankConnections.forEach(bankConnection -> {
            financialGateway.removeConnection(userCredential, bankConnection.getItemId());
            bankConnectionRepository.deleteById(bankConnection.getId());
        });

        userCredentialRepository.delete();
    }
}
