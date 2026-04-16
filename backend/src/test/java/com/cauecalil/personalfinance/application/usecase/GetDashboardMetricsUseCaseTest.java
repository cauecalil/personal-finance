package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GetDashboardMetricsResponse;
import com.cauecalil.personalfinance.application.exception.AccountNotFoundException;
import com.cauecalil.personalfinance.application.exception.FromDateAfterToDateException;
import com.cauecalil.personalfinance.domain.model.valueobject.*;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.AccountJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.CategoryJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.TransactionJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.AccountJpaRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.BankConnectionJpaRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.TransactionJpaRepository;
import com.cauecalil.personalfinance.support.H2UseCaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetDashboardMetricsUseCaseTest extends H2UseCaseIntegrationTest {

    @Autowired
    private GetDashboardMetricsUseCase useCase;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private BankConnectionJpaRepository bankConnectionJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Test
    void shouldThrowFromDateAfterToDateException_whenFromIsAfterTo() {
        Instant from = Instant.parse("2026-04-02T00:00:00Z");
        Instant to = Instant.parse("2026-04-01T00:00:00Z");

        assertThatThrownBy(() -> useCase.execute(null, from, to))
                .isInstanceOf(FromDateAfterToDateException.class);
    }

    @Test
    void shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        assertThatThrownBy(() -> useCase.execute("acc-missing", from, to))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldUseAccountBalanceAndCurrency_whenAccountFilterExists() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("metrics-account-filter");
        AccountJpaEntity account = persistAccount("acc-1", connection, AccountType.BANK, new BigDecimal("1234.56"), "USD");
        CategoryJpaEntity category = persistCategory("cat-metrics", "General", null, null);

        persistTransaction(
                "tx-income",
                account,
                category,
                TransactionType.CREDIT,
                MovementClass.REGULAR,
                new BigDecimal("9000.00"),
                null,
                Instant.parse("2026-04-10T10:00:00Z")
        );

        persistTransaction(
                "tx-expense",
                account,
                category,
                TransactionType.DEBIT,
                MovementClass.REGULAR,
                new BigDecimal("3500.00"),
                null,
                Instant.parse("2026-04-12T10:00:00Z")
        );

        GetDashboardMetricsResponse response = useCase.execute("acc-1", from, to);

        assertThat(response.currentBalance()).isEqualByComparingTo("1234.56");
        assertThat(response.totalIncome()).isEqualByComparingTo("9000.00");
        assertThat(response.totalExpenses()).isEqualByComparingTo("3500.00");
        assertThat(response.currencyCode()).isEqualTo("USD");
    }

    @Test
    void shouldUseBankBalanceSumAndDefaultCurrency_whenAccountFilterIsNull() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("metrics-global");
        AccountJpaEntity bankA = persistAccount("acc-bank-a", connection, AccountType.BANK, new BigDecimal("300.00"), "BRL");
        AccountJpaEntity bankB = persistAccount("acc-bank-b", connection, AccountType.BANK, new BigDecimal("200.00"), "BRL");
        persistAccount("acc-credit", connection, AccountType.CREDIT, new BigDecimal("900.00"), "BRL");

        CategoryJpaEntity category = persistCategory("cat-global", "General", null, null);

        persistTransaction(
                "tx-global-income",
                bankA,
                category,
                TransactionType.CREDIT,
                MovementClass.REGULAR,
                new BigDecimal("100.00"),
                null,
                Instant.parse("2026-04-05T10:00:00Z")
        );

        persistTransaction(
                "tx-global-expense",
                bankB,
                category,
                TransactionType.DEBIT,
                MovementClass.REGULAR,
                new BigDecimal("40.00"),
                null,
                Instant.parse("2026-04-06T10:00:00Z")
        );

        persistTransaction(
                "tx-internal-transfer",
                bankA,
                category,
                TransactionType.DEBIT,
                MovementClass.INTERNAL_TRANSFER,
                new BigDecimal("20.00"),
                null,
                Instant.parse("2026-04-07T10:00:00Z")
        );

        GetDashboardMetricsResponse response = useCase.execute(null, from, to);

        assertThat(response.currentBalance()).isEqualByComparingTo("500.00");
        assertThat(response.totalIncome()).isEqualByComparingTo("100.00");
        assertThat(response.totalExpenses()).isEqualByComparingTo("40.00");
        assertThat(response.currencyCode()).isEqualTo("BRL");
    }

    private BankConnectionJpaEntity persistBankConnection(String suffix) {
        return bankConnectionJpaRepository.save(
                BankConnectionJpaEntity.builder()
                        .itemId(UUID.randomUUID() + "-" + suffix)
                        .bankName("Test Bank")
                        .status(BankConnectionStatus.UPDATED)
                        .build()
        );
    }

    private AccountJpaEntity persistAccount(
            String id,
            BankConnectionJpaEntity bankConnection,
            AccountType type,
            BigDecimal balance,
            String currency
    ) {
        AccountSubType subType = type == AccountType.BANK
                ? AccountSubType.CHECKING_ACCOUNT
                : AccountSubType.CREDIT_CARD;

        return accountJpaRepository.save(
                AccountJpaEntity.builder()
                        .id(id)
                        .bankConnection(bankConnection)
                        .name("Account " + id)
                        .marketingName(null)
                        .type(type)
                        .subType(subType)
                        .number("000" + Math.abs(id.hashCode()))
                        .owner("Test Owner")
                        .taxNumber("12345678901")
                        .balance(balance)
                        .currency(currency)
                        .build()
        );
    }

    private CategoryJpaEntity persistCategory(String id, String description, String descriptionTranslated, String rootCategoryId) {
        return categoryJpaRepository.save(
                CategoryJpaEntity.builder()
                        .id(id)
                        .description(description)
                        .descriptionTranslated(descriptionTranslated)
                        .parentId(null)
                        .rootCategoryId(rootCategoryId)
                        .build()
        );
    }

    private void persistTransaction(
            String id,
            AccountJpaEntity account,
            CategoryJpaEntity category,
            TransactionType type,
            MovementClass movementClass,
            BigDecimal amount,
            BigDecimal amountInAccountCurrency,
            Instant occurredAt
    ) {
        transactionJpaRepository.save(
                TransactionJpaEntity.builder()
                        .id(id)
                        .account(account)
                        .description("Transaction " + id)
                        .currency("BRL")
                        .amount(amount)
                        .amountInAccountCurrency(amountInAccountCurrency)
                        .type(type)
                        .movementClass(movementClass)
                        .category(category)
                        .occurredAt(occurredAt)
                        .build()
        );
    }
}
