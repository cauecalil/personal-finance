package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.request.AddBankConnectionRequest;
import com.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import com.cauecalil.personalfinance.application.exception.BankConnectionAlreadyExistsException;
import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import lombok.RequiredArgsConstructor;
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
                .status(BankConnectionStatus.PENDING)
                .build();

        BankConnection savedBankConnection = bankConnectionRepository.save(bankConnection);

        return BankConnectionResponse.builder()
                .id(savedBankConnection.getId())
                .itemId(savedBankConnection.getItemId())
                .bankName(savedBankConnection.getBankName())
                .status(savedBankConnection.getStatus())
                .lastSyncAt(savedBankConnection.getLastSyncAt())
                .build();
    }
}
