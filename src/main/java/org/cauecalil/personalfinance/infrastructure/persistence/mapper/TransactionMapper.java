package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.valueobject.*;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;

public class TransactionMapper {
    public static TransactionJpaEntity toEntity(Transaction transaction) {
        return TransactionJpaEntity.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .date(transaction.getDate())
                .category(transaction.getCategory())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public static Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .type(TransactionType.valueOf(entity.getType()))
                .date(entity.getDate())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
