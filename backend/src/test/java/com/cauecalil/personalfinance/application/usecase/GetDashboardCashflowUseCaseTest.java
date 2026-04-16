package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GetDashboardCashflowResponse;
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
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetDashboardCashflowUseCaseTest extends H2UseCaseIntegrationTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Autowired
    private GetDashboardCashflowUseCase useCase;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private BankConnectionJpaRepository bankConnectionJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Test
    void shouldFillMissingDailyPeriodsWithZero_whenRangeSpansMultipleDays() {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-03T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("cashflow-fill");
        AccountJpaEntity account = persistAccount("acc-cashflow", connection);
        CategoryJpaEntity category = persistCategory("cat-cashflow", "General");

        persistTransaction("tx-1", account, category, TransactionType.CREDIT, MovementClass.REGULAR, new BigDecimal("100.00"), null, Instant.parse("2026-03-01T08:00:00Z"));
        persistTransaction("tx-2", account, category, TransactionType.DEBIT, MovementClass.REGULAR, new BigDecimal("40.00"), null, Instant.parse("2026-03-01T10:00:00Z"));
        persistTransaction("tx-3", account, category, TransactionType.CREDIT, MovementClass.REGULAR, new BigDecimal("35.50"), null, Instant.parse("2026-03-03T08:00:00Z"));
        persistTransaction("tx-4", account, category, TransactionType.DEBIT, MovementClass.REGULAR, new BigDecimal("10.10"), null, Instant.parse("2026-03-03T10:00:00Z"));

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertThat(response.granularity()).isEqualTo(GetDashboardCashflowResponse.Granularity.DAILY);
        assertThat(response.points()).hasSize(3);

        assertThat(response.points().get(0).incomeTotal()).isEqualByComparingTo("100.00");
        assertThat(response.points().get(0).expensesTotal()).isEqualByComparingTo("40.00");
        assertThat(response.points().get(1).incomeTotal()).isEqualByComparingTo("0");
        assertThat(response.points().get(1).expensesTotal()).isEqualByComparingTo("0");
        assertThat(response.points().get(2).incomeTotal()).isEqualByComparingTo("35.50");
        assertThat(response.points().get(2).expensesTotal()).isEqualByComparingTo("10.10");
    }

    @Test
    void shouldUseAbsoluteTotals_whenRepositoryStoresSignedValues() {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-01T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("cashflow-abs");
        AccountJpaEntity account = persistAccount("acc-cashflow-abs", connection);
        CategoryJpaEntity category = persistCategory("cat-cashflow-abs", "General");

        persistTransaction("tx-abs-1", account, category, TransactionType.CREDIT, MovementClass.REGULAR, new BigDecimal("-250.00"), null, Instant.parse("2026-03-01T08:00:00Z"));
        persistTransaction("tx-abs-2", account, category, TransactionType.DEBIT, MovementClass.REGULAR, new BigDecimal("-120.75"), null, Instant.parse("2026-03-01T09:00:00Z"));

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertThat(response.points()).hasSize(1);
        assertThat(response.points().getFirst().incomeTotal()).isEqualByComparingTo("250.00");
        assertThat(response.points().getFirst().expensesTotal()).isEqualByComparingTo("120.75");
    }

    @Test
    void shouldUseWeeklyGranularity_whenRangeHasMoreThan31Days() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-01T00:00:00Z");

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertThat(response.granularity()).isEqualTo(GetDashboardCashflowResponse.Granularity.WEEKLY);
    }

    @Test
    void shouldUseMonthlyGranularity_whenRangeHasMoreThan180Days() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertThat(response.granularity()).isEqualTo(GetDashboardCashflowResponse.Granularity.MONTHLY);
    }

    @Test
    void shouldUseYearlyGranularity_whenRangeHasMoreThan730Days() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-12-31T23:59:59Z");

        GetDashboardCashflowResponse response = useCase.execute(null, from, to, UTC);

        assertThat(response.granularity()).isEqualTo(GetDashboardCashflowResponse.Granularity.YEARLY);
    }

    @Test
    void shouldThrowFromDateAfterToDateException_whenFromIsAfterTo() {
        Instant from = Instant.parse("2026-04-02T00:00:00Z");
        Instant to = Instant.parse("2026-04-01T00:00:00Z");

        assertThatThrownBy(() -> useCase.execute(null, from, to, UTC))
                .isInstanceOf(FromDateAfterToDateException.class);
    }

    @Test
    void shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        Instant from = Instant.parse("2026-03-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-31T23:59:59Z");

        assertThatThrownBy(() -> useCase.execute("missing", from, to, UTC))
                .isInstanceOf(AccountNotFoundException.class);
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

    private CategoryJpaEntity persistCategory(String id, String description) {
        return categoryJpaRepository.save(
                CategoryJpaEntity.builder()
                        .id(id)
                        .description(description)
                        .descriptionTranslated(null)
                        .parentId(null)
                        .rootCategoryId(null)
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

