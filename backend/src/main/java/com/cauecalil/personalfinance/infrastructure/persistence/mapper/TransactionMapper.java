package com.cauecalil.personalfinance.infrastructure.persistence.mapper;

import com.cauecalil.personalfinance.domain.model.Transaction;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;

public class TransactionMapper {
    public static TransactionJpaEntity toEntity(Transaction transaction, AccountJpaEntity account, CategoryJpaEntity category) {
        return TransactionJpaEntity.builder()
                .id(transaction.getId())
                .account(account)
                .description(transaction.getDescription())
                .currency(transaction.getCurrency())
                .amount(transaction.getAmount())
                .amountInAccountCurrency(transaction.getAmountInAccountCurrency())
                .type(transaction.getType())
                .movementClass(transaction.getMovementClass())
                .category(category)
                .occurredAt(transaction.getOccurredAt())
                .build();
    }

    public static Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccount().getId())
                .description(entity.getDescription())
                .currency(entity.getCurrency())
                .amount(entity.getAmount())
                .amountInAccountCurrency(entity.getAmountInAccountCurrency())
                .type(entity.getType())
                .movementClass(entity.getMovementClass())
                .categoryId(entity.getCategory().getId())
                .occurredAt(entity.getOccurredAt())
                .build();
    }
}
