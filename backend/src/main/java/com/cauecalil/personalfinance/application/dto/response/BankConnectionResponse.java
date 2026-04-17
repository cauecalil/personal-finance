package com.cauecalil.personalfinance.application.dto.response;

import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(description = "Represents a configured bank connection and its latest synchronization status.")
public record BankConnectionResponse(
    @Schema(description = "Internal numeric identifier of the bank connection.", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
    @Schema(description = "Pluggy item identifier for the connected institution in UUID format.", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        String itemId,
    @Schema(description = "Display name of the connected bank.", example = "Itaú", requiredMode = Schema.RequiredMode.REQUIRED)
        String bankName,
    @Schema(description = "Current synchronization status. Possible values: PENDING, UPDATED, ERROR.", example = "UPDATED", requiredMode = Schema.RequiredMode.REQUIRED)
        BankConnectionStatus status,
    @Schema(description = "Timestamp of the latest synchronization attempt in ISO 8601 instant format.", example = "2026-04-16T13:45:22Z", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
