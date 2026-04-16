package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.ListTransactionsResponse;
import com.cauecalil.personalfinance.application.dto.response.TransactionResponse;
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
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ListTransactionsUseCaseTest extends H2UseCaseIntegrationTest {

    @Autowired
    private ListTransactionsUseCase useCase;

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
        Instant from = Instant.parse("2026-04-03T00:00:00Z");
        Instant to = Instant.parse("2026-04-01T00:00:00Z");

        assertThatThrownBy(() -> useCase.execute(null, from, to, 0, 20, Sort.Direction.DESC))
                .isInstanceOf(FromDateAfterToDateException.class);
    }

    @Test
    void shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        assertThatThrownBy(() -> useCase.execute("acc-missing", from, to, 0, 20, Sort.Direction.ASC))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldListTransactionsForSpecificAccount_whenAccountFilterIsProvided() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("list-by-account");
        AccountJpaEntity account = persistAccount("acc-1", connection);

        CategoryJpaEntity category1 = persistCategory("cat-1", "Food", "Alimentacao");
        CategoryJpaEntity category2 = persistCategory("cat-2", "Transport", null);

        persistTransaction("tx-1", account, category1, "Grocery", TransactionType.DEBIT, new BigDecimal("120.00"), new BigDecimal("120.00"), Instant.parse("2026-04-05T12:00:00Z"));
        persistTransaction("tx-2", account, category2, "Bus", TransactionType.DEBIT, new BigDecimal("10.00"), new BigDecimal("10.00"), Instant.parse("2026-04-04T07:00:00Z"));
        persistTransaction("tx-3", account, category1, "Coffee", TransactionType.DEBIT, new BigDecimal("8.00"), new BigDecimal("8.00"), Instant.parse("2026-04-03T07:00:00Z"));

        ListTransactionsResponse response = useCase.execute(account.getId(), from, to, 0, 2, Sort.Direction.DESC);

        assertThat(response.page()).isEqualTo(0);
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.totalItems()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNextPage()).isTrue();
        assertThat(response.hasPreviousPage()).isFalse();

        assertThat(response.items())
                .extracting(TransactionResponse::id, TransactionResponse::type, TransactionResponse::category)
                .containsExactly(
                        tuple("tx-1", "DEBIT", "Alimentacao"),
                        tuple("tx-2", "DEBIT", "Transport")
                );
    }

    @Test
    void shouldListTransactionsWithoutAccountFilter_whenAccountIdIsNull() {
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-30T23:59:59Z");

        BankConnectionJpaEntity connection = persistBankConnection("list-global");
        AccountJpaEntity accountOne = persistAccount("acc-2", connection);
        AccountJpaEntity accountTwo = persistAccount("acc-3", connection);
        CategoryJpaEntity category = persistCategory("cat-3", "Salary", "Salario");

        persistTransaction("tx-3", accountOne, category, "Salary", TransactionType.CREDIT, new BigDecimal("5000.00"), new BigDecimal("5000.00"), Instant.parse("2026-04-03T09:00:00Z"));
        persistTransaction("tx-out", accountTwo, category, "Old Salary", TransactionType.CREDIT, new BigDecimal("1000.00"), new BigDecimal("1000.00"), Instant.parse("2026-03-03T09:00:00Z"));

        ListTransactionsResponse response = useCase.execute(null, from, to, 0, 10, Sort.Direction.ASC);

        assertThat(response.items())
                .extracting(TransactionResponse::id, TransactionResponse::type, TransactionResponse::category)
                .containsExactly(tuple("tx-3", "CREDIT", "Salario"));
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

    private CategoryJpaEntity persistCategory(String id, String description, String descriptionTranslated) {
        return categoryJpaRepository.save(
                CategoryJpaEntity.builder()
                        .id(id)
                        .description(description)
                        .descriptionTranslated(descriptionTranslated)
                        .parentId(null)
                        .rootCategoryId(null)
                        .build()
        );
    }

    private void persistTransaction(
            String id,
            AccountJpaEntity account,
            CategoryJpaEntity category,
            String description,
            TransactionType type,
            BigDecimal amount,
            BigDecimal amountInAccountCurrency,
            Instant occurredAt
    ) {
        transactionJpaRepository.save(
                TransactionJpaEntity.builder()
                        .id(id)
                        .account(account)
                        .description(description)
                        .currency("BRL")
                        .amount(amount)
                        .amountInAccountCurrency(amountInAccountCurrency)
                        .type(type)
                        .movementClass(MovementClass.REGULAR)
                        .category(category)
                        .occurredAt(occurredAt)
                        .build()
        );
    }
}
