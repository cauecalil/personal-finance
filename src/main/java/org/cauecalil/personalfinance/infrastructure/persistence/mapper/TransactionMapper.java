package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.valueobject.*;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;

public class TransactionMapper {
    public static TransactionJpaEntity toEntity(Transaction transaction) {
        AccountJpaEntity accountEntity = AccountJpaEntity.builder()
                .id(transaction.getAccountId())
                .build();

        return TransactionJpaEntity.builder()
                .id(transaction.getId())
                .account(accountEntity)
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .category(transaction.getCategory())
                .occurredAt(transaction.getOccurredAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public static Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccount().getId())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .type(TransactionType.valueOf(entity.getType()))
                .category(entity.getCategory())
                .occurredAt(entity.getOccurredAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
