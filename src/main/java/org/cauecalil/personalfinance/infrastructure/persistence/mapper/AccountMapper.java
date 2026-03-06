package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.valueobject.Currency;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;

public class AccountMapper {
    public static AccountJpaEntity toEntity(Account account) {
        BankConnectionJpaEntity bankConnectionEntity = BankConnectionJpaEntity.builder()
                .id(account.getBankConnectionId())
                .build();

        return AccountJpaEntity.builder()
                .id(account.getId())
                .bankConnection(bankConnectionEntity)
                .name(account.getName())
                .type(account.getType())
                .subtype(account.getSubtype())
                .balance(account.getBalance())
                .currency(account.getCurrency().name())
                .build();
    }

    public static Account toDomain(AccountJpaEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .bankConnectionId(entity.getBankConnection().getId())
                .name(entity.getName())
                .type(entity.getType())
                .subtype(entity.getSubtype())
                .balance(entity.getBalance())
                .currency(Currency.valueOf(entity.getCurrency()))
                .build();
    }
}
