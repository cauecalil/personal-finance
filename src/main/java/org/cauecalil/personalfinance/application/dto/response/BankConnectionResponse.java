package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;
import org.cauecalil.personalfinance.domain.model.BankConnection;

import java.time.LocalDateTime;

@Builder
public record BankConnectionResponse(
        Long id,
        String itemId,
        String bankName,
        String status,
        LocalDateTime lastSyncAt,
        LocalDateTime createdAt
) {
    public static BankConnectionResponse from(BankConnection bankConnection) {
        return BankConnectionResponse.builder()
                .id(bankConnection.getId())
                .itemId(bankConnection.getItemId())
                .bankName(bankConnection.getBankName())
                .status(bankConnection.getStatus())
                .lastSyncAt(bankConnection.getLastSyncAt())
                .createdAt(bankConnection.getCreatedAt())
                .build();
    }
}
