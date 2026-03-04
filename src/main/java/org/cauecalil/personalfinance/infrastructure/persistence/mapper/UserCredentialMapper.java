package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.UserCredentialJpaEntity;

public class UserCredentialMapper {
    public static UserCredentialJpaEntity toEntity(UserCredential userCredential) {
        return UserCredentialJpaEntity.builder()
                .id(userCredential.getId())
                .clientId(userCredential.getClientId())
                .clientSecret(userCredential.getClientSecret())
                .createdAt(userCredential.getCreatedAt())
                .lastSyncAt(userCredential.getLastSyncAt())
                .build();
    }

    public static UserCredential toDomain(UserCredentialJpaEntity entity) {
        return UserCredential.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
                .clientSecret(entity.getClientSecret())
                .createdAt(entity.getCreatedAt())
                .lastSyncAt(entity.getLastSyncAt())
                .build();
    }
}
