package com.cauecalil.personalfinance.infrastructure.persistence.mapper;

import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;

public class BankConnectionMapper {
    public static BankConnectionJpaEntity toEntity(BankConnection bankConnection) {
        return BankConnectionJpaEntity.builder()
                .id(bankConnection.getId())
                .itemId(bankConnection.getItemId())
                .bankName(bankConnection.getBankName())
                .status(bankConnection.getStatus())
                .lastSyncAt(bankConnection.getLastSyncAt())
                .build();
    }

    public static BankConnection toDomain(BankConnectionJpaEntity entity) {
        return BankConnection.builder()
                .id(entity.getId())
                .itemId(entity.getItemId())
                .bankName(entity.getBankName())
                .status(entity.getStatus())
                .lastSyncAt(entity.getLastSyncAt())
                .build();
    }
}
