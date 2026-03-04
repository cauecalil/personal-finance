package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.valueobject.Currency;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;

public class AccountMapper {
    public static AccountJpaEntity toEntity(Account account) {
        return AccountJpaEntity.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .subtype(account.getSubtype())
                .balance(account.getBalance())
                .currency(account.getCurrency().name())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public static Account toDomain(AccountJpaEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .subtype(entity.getSubtype())
                .balance(entity.getBalance())
                .currency(Currency.valueOf(entity.getCurrency()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
