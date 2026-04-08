package com.cauecalil.personalfinance.domain.model;

import com.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {
    private String id;
    private Long bankConnectionId;
    private String name;
    private String marketingName;
    private AccountType type;
    private AccountSubType subType;
    private String number;
    private String owner;
    private String taxNumber;
    private BigDecimal balance;
    private String currency;
}
