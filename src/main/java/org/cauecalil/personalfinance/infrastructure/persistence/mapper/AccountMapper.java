package org.cauecalil.personalfinance.infrastructure.persistence.mapper;

import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;

public class AccountMapper {
    public static AccountJpaEntity toEntity(Account account, BankConnectionJpaEntity bankConnection) {
        return AccountJpaEntity.builder()
                .id(account.getId())
                .bankConnection(bankConnection)
                .name(account.getName())
                .marketingName(account.getMarketingName())
                .type(account.getType())
                .subType(account.getSubType())
                .number(account.getNumber())
                .owner(account.getOwner())
                .taxNumber(account.getTaxNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }

    public static Account toDomain(AccountJpaEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .bankConnectionId(entity.getBankConnection().getId())
                .name(entity.getName())
                .marketingName(entity.getMarketingName())
                .type(entity.getType())
                .subType(entity.getSubType())
                .number(entity.getNumber())
                .owner(entity.getOwner())
                .taxNumber(entity.getTaxNumber())
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .build();
    }
}
