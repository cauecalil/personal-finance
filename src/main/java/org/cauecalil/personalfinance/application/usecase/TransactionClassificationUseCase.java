package org.cauecalil.personalfinance.application.usecase;

import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.model.Category;
import org.cauecalil.personalfinance.domain.model.Transaction;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import org.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import org.cauecalil.personalfinance.domain.model.valueobject.MovementClass;
import org.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionClassificationUseCase {
    private static final Duration CREDIT_CARD_PAYMENT_PAIR_WINDOW = Duration.ofDays(3);

    public List<Transaction> execute(
            List<Transaction> transactions,
            List<Account> accounts,
            List<Category> categories
    ) {
        Map<String, Account> accountById = accounts.stream()
                .collect(Collectors.toMap(Account::getId, account -> account));

        Map<String, Category> categoryById = categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        Set<String> internalTransferByCategory = new HashSet<>();
        for (Category category : categories) {
            if (isInternalTransferLabel(category.getDescription()) || isInternalTransferLabel(category.getDescriptionTranslated())) {
                internalTransferByCategory.add(category.getId());
            }
        }

        Set<String> internalTransferIds = new HashSet<>();
        for (Transaction transaction : transactions) {
            if (isInternalTransferByCategory(transaction, categoryById, internalTransferByCategory)) {
                internalTransferIds.add(transaction.getId());
            }
        }

        internalTransferIds.addAll(matchCreditCardBillPayments(transactions, accountById));

        return transactions.stream()
                .map(transaction -> {
                    boolean isInternalTransfer = internalTransferIds.contains(transaction.getId());

                    return transaction.toBuilder()
                            .movementClass(isInternalTransfer ? MovementClass.INTERNAL_TRANSFER : MovementClass.REGULAR)
                            .build();
                })
                .toList();
    }

    private boolean isInternalTransferByCategory(
            Transaction transaction,
            Map<String, Category> categoryById,
            Set<String> internalTransferByCategory
    ) {
        Category category = categoryById.get(transaction.getCategoryId());
        if (category == null) {
            return false;
        }

        if (internalTransferByCategory.contains(category.getId())) {
            return true;
        }

        return category.getRootCategoryId() != null && internalTransferByCategory.contains(category.getRootCategoryId());
    }

    private Set<String> matchCreditCardBillPayments(
            List<Transaction> transactions,
            Map<String, Account> accountById
    ) {
        Map<BigDecimal, List<Transaction>> bankDebitsByAmount = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (!isBankDebit(transaction, accountById)) {
                continue;
            }

            BigDecimal absoluteAmount = normalizeAmount(transaction);
            bankDebitsByAmount.computeIfAbsent(absoluteAmount, ignored -> new ArrayList<>()).add(transaction);
        }

        Set<String> matchedIds = new HashSet<>();

        for (Transaction transaction : transactions) {
            if (!isCreditCardCredit(transaction, accountById)) {
                continue;
            }

            BigDecimal absoluteAmount = normalizeAmount(transaction);
            List<Transaction> candidates = bankDebitsByAmount.getOrDefault(absoluteAmount, List.of());
            Transaction bestMatch = findClosestByDate(transaction, candidates);

            if (bestMatch != null) {
                matchedIds.add(transaction.getId());
                matchedIds.add(bestMatch.getId());
            }
        }

        return matchedIds;
    }

    private Transaction findClosestByDate(Transaction target, List<Transaction> candidates) {
        Transaction closest = null;
        long closestDiff = Long.MAX_VALUE;

        for (Transaction candidate : candidates) {
            long diff = Math.abs(Duration.between(target.getOccurredAt(), candidate.getOccurredAt()).toHours());
            if (diff <= CREDIT_CARD_PAYMENT_PAIR_WINDOW.toHours() && diff < closestDiff) {
                closestDiff = diff;
                closest = candidate;
            }
        }

        return closest;
    }

    private boolean isBankDebit(Transaction transaction, Map<String, Account> accountById) {
        Account account = accountById.get(transaction.getAccountId());

        return account != null
                && account.getType() == AccountType.BANK
                && transaction.getType() == TransactionType.DEBIT;
    }

    private boolean isCreditCardCredit(Transaction transaction, Map<String, Account> accountById) {
        Account account = accountById.get(transaction.getAccountId());

        return account != null
                && account.getSubType() == AccountSubType.CREDIT_CARD
                && transaction.getType() == TransactionType.CREDIT;
    }

    private BigDecimal normalizeAmount(Transaction transaction) {
        BigDecimal effectiveAmount = Optional.ofNullable(transaction.getAmountInAccountCurrency())
                .orElse(transaction.getAmount());

        if (effectiveAmount == null) {
            return BigDecimal.ZERO;
        }

        return effectiveAmount.abs().stripTrailingZeros();
    }

    private boolean isInternalTransferLabel(String label) {
        if (label == null || label.isBlank()) {
            return false;
        }

        String normalized = label.toLowerCase(Locale.ROOT);

        return normalized.contains("same person transfer")
                || normalized.contains("transfer - internal")
                || normalized.contains("credit card payment")
                || normalized.contains("mesma titularidade")
                || normalized.contains("transferencia interna")
                || normalized.contains("pagamento de fatura");
    }
}




