package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.AccountResponse;
import com.cauecalil.personalfinance.domain.model.Account;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import com.cauecalil.personalfinance.domain.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAccountsUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ListAccountsUseCase useCase;

    @Test
    void shouldMapAccountsUsingMarketingNameWhenAvailable() {
        Account first = Account.builder()
                .id("acc-1")
                .name("Conta Corrente")
                .marketingName("Conta Premium")
                .type(AccountType.BANK)
                .subType(AccountSubType.CHECKING_ACCOUNT)
                .balance(new BigDecimal("100.50"))
                .currency("BRL")
                .build();

        Account second = Account.builder()
                .id("acc-2")
                .name("Savings")
                .marketingName(null)
                .type(AccountType.BANK)
                .subType(AccountSubType.SAVINGS_ACCOUNT)
                .balance(new BigDecimal("2500.00"))
                .currency("USD")
                .build();

        when(accountRepository.findAll()).thenReturn(List.of(first, second));

        List<AccountResponse> response = useCase.execute();

        assertThat(response)
                .extracting(
                        AccountResponse::id,
                        AccountResponse::name,
                        AccountResponse::type,
                        AccountResponse::subtype,
                        AccountResponse::balance,
                        AccountResponse::currency
                )
                .containsExactly(
                        tuple("acc-1", "Conta Premium", "BANK", "CHECKING_ACCOUNT", new BigDecimal("100.50"), "BRL"),
                        tuple("acc-2", "Savings", "BANK", "SAVINGS_ACCOUNT", new BigDecimal("2500.00"), "USD")
                );

        verify(accountRepository).findAll();
    }
}
