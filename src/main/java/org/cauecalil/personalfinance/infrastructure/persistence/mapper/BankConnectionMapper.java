package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;

public class BankConnectionMapper {
    public static BankConnectionJpaEntity toEntity(BankConnection bankConnection) {
        return BankConnectionJpaEntity.builder()
                .id(bankConnection.getId())
                .itemId(bankConnection.getItemId())
                .bankName(bankConnection.getBankName())
                .status(bankConnection.getStatus())
                .lastSyncAt(bankConnection.getLastSyncAt())
                .createdAt(bankConnection.getCreatedAt())
                .build();
    }

    public static BankConnection toDomain(BankConnectionJpaEntity entity) {
        return BankConnection.builder()
                .id(entity.getId())
                .itemId(entity.getItemId())
                .bankName(entity.getBankName())
                .status(entity.getStatus())
                .lastSyncAt(entity.getLastSyncAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
