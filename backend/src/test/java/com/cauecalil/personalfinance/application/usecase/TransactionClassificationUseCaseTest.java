package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.domain.model.Account;
import com.cauecalil.personalfinance.domain.model.Category;
import com.cauecalil.personalfinance.domain.model.Transaction;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import com.cauecalil.personalfinance.domain.model.valueobject.MovementClass;
import com.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TransactionClassificationUseCaseTest {

    @InjectMocks
    private TransactionClassificationUseCase useCase;

    @Test
    void shouldMarkRegularWhenNoInternalTransferRuleMatches() {
        Account creditCard = account("acc-card", AccountType.CREDIT, AccountSubType.CREDIT_CARD);

        Transaction transaction = Transaction.builder()
                .id("tx-1")
                .accountId("acc-card")
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("-150.00"))
                .occurredAt(Instant.parse("2026-03-01T12:00:00Z"))
                .build();

        Transaction classified = useCase.execute(List.of(transaction), List.of(creditCard), List.of()).getFirst();

        assertThat(classified.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(classified.getMovementClass()).isEqualTo(MovementClass.REGULAR);
    }

    @Test
    void shouldMarkInternalTransferWhenCategoryRootHasInternalTransferLabel() {
        Account bank = account("acc-bank", AccountType.BANK, AccountSubType.CHECKING_ACCOUNT);

        Category rootCategory = Category.builder()
                .id("05000000")
                .description("Transfer - internal")
                .rootCategoryId("05000000")
                .build();

        Category childCategory = Category.builder()
                .id("05010000")
                .description("Any child")
                .rootCategoryId("05000000")
                .build();

        Transaction transaction = Transaction.builder()
                .id("tx-2")
                .accountId("acc-bank")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("320.00"))
                .categoryId("05010000")
                .occurredAt(Instant.parse("2026-03-02T10:00:00Z"))
                .build();

        Transaction classified = useCase
                .execute(List.of(transaction), List.of(bank), List.of(rootCategory, childCategory))
                .getFirst();

        assertThat(classified.getMovementClass()).isEqualTo(MovementClass.INTERNAL_TRANSFER);
    }

    @Test
    void shouldMarkBothTransactionsAsInternalTransferWhenCreditCardPaymentPairMatches() {
        Account bank = account("acc-bank", AccountType.BANK, AccountSubType.CHECKING_ACCOUNT);
        Account creditCard = account("acc-card", AccountType.CREDIT, AccountSubType.CREDIT_CARD);

        Transaction bankDebit = Transaction.builder()
                .id("tx-bank")
                .accountId("acc-bank")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("500.00"))
                .occurredAt(Instant.parse("2026-03-10T14:00:00Z"))
                .build();

        Transaction cardCredit = Transaction.builder()
                .id("tx-card")
                .accountId("acc-card")
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("-500.00"))
                .occurredAt(Instant.parse("2026-03-10T16:00:00Z"))
                .build();

        Map<String, Transaction> byId = useCase
                .execute(List.of(bankDebit, cardCredit), List.of(bank, creditCard), List.of())
                .stream()
                .collect(Collectors.toMap(Transaction::getId, Function.identity()));

        assertThat(byId.get("tx-bank").getMovementClass()).isEqualTo(MovementClass.INTERNAL_TRANSFER);
        assertThat(byId.get("tx-card").getMovementClass()).isEqualTo(MovementClass.INTERNAL_TRANSFER);
    }

    @Test
    void shouldKeepTransactionsRegularWhenCreditCardPaymentPairIsOutsideMatchingWindow() {
        Account bank = account("acc-bank", AccountType.BANK, AccountSubType.CHECKING_ACCOUNT);
        Account creditCard = account("acc-card", AccountType.CREDIT, AccountSubType.CREDIT_CARD);

        Transaction bankDebit = Transaction.builder()
                .id("tx-bank")
                .accountId("acc-bank")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("500.00"))
                .occurredAt(Instant.parse("2026-03-10T14:00:00Z"))
                .build();

        Transaction cardCredit = Transaction.builder()
                .id("tx-card")
                .accountId("acc-card")
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("-500.00"))
                .occurredAt(Instant.parse("2026-03-16T16:00:00Z"))
                .build();

        Map<String, Transaction> byId = useCase
                .execute(List.of(bankDebit, cardCredit), List.of(bank, creditCard), List.of())
                .stream()
                .collect(Collectors.toMap(Transaction::getId, Function.identity()));

        assertThat(byId.get("tx-bank").getMovementClass()).isEqualTo(MovementClass.REGULAR);
        assertThat(byId.get("tx-card").getMovementClass()).isEqualTo(MovementClass.REGULAR);
    }

    @Test
    void shouldUseAmountInAccountCurrencyWhenMatchingCreditCardPaymentPair() {
        Account bank = account("acc-bank", AccountType.BANK, AccountSubType.CHECKING_ACCOUNT);
        Account creditCard = account("acc-card", AccountType.CREDIT, AccountSubType.CREDIT_CARD);

        Transaction bankDebit = Transaction.builder()
                .id("tx-bank")
                .accountId("acc-bank")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("100.00"))
                .amountInAccountCurrency(new BigDecimal("50.00"))
                .occurredAt(Instant.parse("2026-03-10T14:00:00Z"))
                .build();

        Transaction cardCredit = Transaction.builder()
                .id("tx-card")
                .accountId("acc-card")
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("-50.00"))
                .occurredAt(Instant.parse("2026-03-10T15:00:00Z"))
                .build();

        Map<String, Transaction> byId = useCase
                .execute(List.of(bankDebit, cardCredit), List.of(bank, creditCard), List.of())
                .stream()
                .collect(Collectors.toMap(Transaction::getId, Function.identity()));

        assertThat(byId.get("tx-bank").getMovementClass()).isEqualTo(MovementClass.INTERNAL_TRANSFER);
        assertThat(byId.get("tx-card").getMovementClass()).isEqualTo(MovementClass.INTERNAL_TRANSFER);
    }

    private Account account(String id, AccountType type, AccountSubType subType) {
        return Account.builder()
                .id(id)
                .type(type)
                .subType(subType)
                .build();
    }
}




