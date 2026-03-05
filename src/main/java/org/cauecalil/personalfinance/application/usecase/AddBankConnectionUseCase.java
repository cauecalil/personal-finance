package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.request.AddBankConnectionRequest;
import org.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import org.cauecalil.personalfinance.application.exception.BankConnectionAlreadyExistsException;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddBankConnectionUseCase {
    private final BankConnectionRepository bankConnectionRepository;

    public BankConnectionResponse execute(AddBankConnectionRequest request) {
        if (bankConnectionRepository.existsByItemId(request.itemId())) {
            throw new BankConnectionAlreadyExistsException();
        }

        BankConnection bankConnection = BankConnection.builder()
                .itemId(request.itemId())
                .bankName(request.bankName())
                .status("PENDING")
                .build();

        BankConnection savedBankConnection = bankConnectionRepository.save(bankConnection);

        return BankConnectionResponse.builder()
                .id(savedBankConnection.getId())
                .itemId(savedBankConnection.getItemId())
                .bankName(savedBankConnection.getBankName())
                .status(savedBankConnection.getStatus())
                .lastSyncAt(savedBankConnection.getLastSyncAt())
                .createdAt(savedBankConnection.getCreatedAt())
                .build();
    }
}
