package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;

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
                .categoryId(entity.getCategory().getId())
                .occurredAt(entity.getOccurredAt())
                .build();
    }
}
