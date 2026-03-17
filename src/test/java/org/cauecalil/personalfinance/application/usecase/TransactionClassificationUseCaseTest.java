package org.cauecalil.personalfinance.application.usecase;

import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import org.cauecalil.personalfinance.domain.model.valueobject.MovementClass;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionClassificationUseCaseTest {
    private final TransactionClassificationUseCase useCase = new TransactionClassificationUseCase();

    @Test
    void shouldKeepTransactionTypeAndMarkRegularWhenNoInternalTransferIsDetected() {
        Account creditCard = account("acc-card", AccountType.CREDIT, AccountSubType.CREDIT_CARD);

        Transaction transaction = Transaction.builder()
                .id("tx-1")
                .accountId("acc-card")
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("-150.00"))
                .occurredAt(Instant.parse("2026-03-01T12:00:00Z"))
                .build();

        Transaction normalized = useCase.execute(List.of(transaction), List.of(creditCard), List.of()).getFirst();

        assertEquals(TransactionType.CREDIT, normalized.getType());
        assertEquals(MovementClass.REGULAR, normalized.getMovementClass());
    }

    @Test
    void shouldMarkInternalTransferWhenCategoryIndicatesCreditCardPayment() {
        Account bank = account("acc-bank", AccountType.BANK, AccountSubType.CHECKING_ACCOUNT);

        Category transferRoot = Category.builder()
                .id("05000000")
                .description("Transfers")
                .rootCategoryId("05000000")
                .build();

        Category creditCardPayment = Category.builder()
                .id("05090000")
                .description("Credit card payment")
                .rootCategoryId("05000000")
                .build();

        Transaction transaction = Transaction.builder()
                .id("tx-2")
                .accountId("acc-bank")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("320.00"))
                .categoryId("05090000")
                .occurredAt(Instant.parse("2026-03-02T10:00:00Z"))
                .build();

        Transaction normalized = useCase
                .execute(List.of(transaction), List.of(bank), List.of(transferRoot, creditCardPayment))
                .getFirst();

        assertEquals(MovementClass.INTERNAL_TRANSFER, normalized.getMovementClass());
    }

    @Test
    void shouldMatchCreditCardBillPaymentAcrossAccountsByAmountAndDate() {
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

        assertEquals(MovementClass.INTERNAL_TRANSFER, byId.get("tx-bank").getMovementClass());
        assertEquals(MovementClass.INTERNAL_TRANSFER, byId.get("tx-card").getMovementClass());
    }

    private Account account(String id, AccountType type, AccountSubType subType) {
        return Account.builder()
                .id(id)
                .type(type)
                .subType(subType)
                .build();
    }
}




