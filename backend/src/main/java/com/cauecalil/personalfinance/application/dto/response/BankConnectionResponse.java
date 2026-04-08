package com.cauecalil.personalfinance.application.dto.response;

import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record BankConnectionResponse(
        Long id,
        String itemId,
        String bankName,
        BankConnectionStatus status,
        Instant lastSyncAt
) {
    public static BankConnectionResponse from(BankConnection bankConnection) {
        return BankConnectionResponse.builder()
                .id(bankConnection.getId())
                .itemId(bankConnection.getItemId())
                .bankName(bankConnection.getBankName())
                .status(bankConnection.getStatus())
                .lastSyncAt(bankConnection.getLastSyncAt())
                .build();
    }
}
