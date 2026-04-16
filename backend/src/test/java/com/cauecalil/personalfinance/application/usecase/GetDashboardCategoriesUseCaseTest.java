package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GetDashboardCategoriesResponse;
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

import static org.assertj.core.api.Assertions.*;

class GetDashboardCategoriesUseCaseTest extends H2UseCaseIntegrationTest {

    @Autowired
    private GetDashboardCategoriesUseCase useCase;

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
    void shouldSplitIncomeAndExpenses_whenAggregationsContainBothTypes() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("categories-account-filter");
        AccountJpaEntity account = persistAccount("acc-1", connection);

        CategoryJpaEntity rootExpenses = persistCategory("root-exp", "Household", "Essentials", null, null);
        CategoryJpaEntity food = persistCategory("cat-food", "Food", null, null, rootExpenses.getId());
        CategoryJpaEntity transport = persistCategory("cat-transport", "Transport", null, null, null);
        CategoryJpaEntity salary = persistCategory("cat-salary", "Salary", null, null, null);

        persistTransaction("tx-food", account, food, TransactionType.DEBIT, MovementClass.REGULAR, new BigDecimal("250.00"), Instant.parse("2026-04-05T10:00:00Z"));
        persistTransaction("tx-transport", account, transport, TransactionType.DEBIT, MovementClass.REGULAR, new BigDecimal("90.00"), Instant.parse("2026-04-06T10:00:00Z"));
        persistTransaction("tx-salary", account, salary, TransactionType.CREDIT, MovementClass.REGULAR, new BigDecimal("5000.00"), Instant.parse("2026-04-07T10:00:00Z"));

        GetDashboardCategoriesResponse response = useCase.execute(account.getId(), from, to);

        assertThat(response.expenses())
                .extracting(GetDashboardCategoriesResponse.CategoryResponse::category, GetDashboardCategoriesResponse.CategoryResponse::total)
                .containsExactly(
                        tuple("Essentials", new BigDecimal("250.00")),
                        tuple("Transport", new BigDecimal("90.00"))
                );

        assertThat(response.income())
                .extracting(GetDashboardCategoriesResponse.CategoryResponse::category, GetDashboardCategoriesResponse.CategoryResponse::total)
                .containsExactly(tuple("Salary", new BigDecimal("5000.00")));
    }

    @Test
    void shouldExcludeInternalTransferRows_whenAccountIdIsNull() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("categories-global");
        AccountJpaEntity account = persistAccount("acc-global", connection);
        CategoryJpaEntity food = persistCategory("cat-food-global", "Food", null, null, null);

        persistTransaction("tx-regular", account, food, TransactionType.DEBIT, MovementClass.REGULAR, new BigDecimal("100.00"), Instant.parse("2026-04-08T10:00:00Z"));
        persistTransaction("tx-internal", account, food, TransactionType.DEBIT, MovementClass.INTERNAL_TRANSFER, new BigDecimal("999.00"), Instant.parse("2026-04-09T10:00:00Z"));

        GetDashboardCategoriesResponse response = useCase.execute(null, from, to);

        assertThat(response.expenses())
                .extracting(GetDashboardCategoriesResponse.CategoryResponse::category, GetDashboardCategoriesResponse.CategoryResponse::total)
                .containsExactly(tuple("Food", new BigDecimal("100.00")));

        assertThat(response.income()).isEmpty();
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

    private AccountJpaEntity persistAccount(String id, BankConnectionJpaEntity connection) {
        return accountJpaRepository.save(
                AccountJpaEntity.builder()
                        .id(id)
                        .bankConnection(connection)
                        .name("Account " + id)
                        .marketingName(null)
                        .type(AccountType.BANK)
                        .subType(AccountSubType.CHECKING_ACCOUNT)
                        .number("000" + Math.abs(id.hashCode()))
                        .owner("Test Owner")
                        .taxNumber("12345678901")
                        .balance(new BigDecimal("1000.00"))
                        .currency("BRL")
                        .build()
        );
    }

    private CategoryJpaEntity persistCategory(
            String id,
            String description,
            String descriptionTranslated,
            String parentId,
            String rootCategoryId
    ) {
        return categoryJpaRepository.save(
                CategoryJpaEntity.builder()
                        .id(id)
                        .description(description)
                        .descriptionTranslated(descriptionTranslated)
                        .parentId(parentId)
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
            Instant occurredAt
    ) {
        transactionJpaRepository.save(
                TransactionJpaEntity.builder()
                        .id(id)
                        .account(account)
                        .description("Transaction " + id)
                        .currency("BRL")
                        .amount(amount)
                        .amountInAccountCurrency(null)
                        .type(type)
                        .movementClass(movementClass)
                        .category(category)
                        .occurredAt(occurredAt)
                        .build()
        );
    }
}
