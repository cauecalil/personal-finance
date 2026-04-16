package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.SyncBankDataResponse;
import com.cauecalil.personalfinance.application.exception.BankConnectionNotFoundException;
import com.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import com.cauecalil.personalfinance.application.port.FinancialGateway;
import com.cauecalil.personalfinance.domain.model.*;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountSubType;
import com.cauecalil.personalfinance.domain.model.valueobject.AccountType;
import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import com.cauecalil.personalfinance.domain.model.valueobject.TransactionType;
import com.cauecalil.personalfinance.domain.repository.AccountRepository;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import com.cauecalil.personalfinance.domain.repository.TransactionRepository;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncBankDataUseCaseTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private SyncCategoriesUseCase syncCategoriesUseCase;

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionClassificationUseCase transactionClassificationUseCase;

    @Mock
    private FinancialGateway financialGateway;

    @InjectMocks
    private SyncBankDataUseCase useCase;

    @Test
    void shouldThrowUserCredentialNotFoundExceptionWhenCredentialIsMissing() {
        when(userCredentialRepository.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(UserCredentialNotFoundException.class);

        verifyNoInteractions(syncCategoriesUseCase, bankConnectionRepository, accountRepository, transactionRepository, transactionClassificationUseCase, financialGateway);
    }

    @Test
    void shouldThrowBankConnectionNotFoundExceptionWhenNoConnectionsAreAvailable() {
        UserCredential credential = UserCredential.builder().clientId("id").clientSecret("secret").build();
        List<Category> categories = List.of(Category.builder().id("cat-1").description("Food").build());

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));
        when(syncCategoriesUseCase.execute(credential)).thenReturn(categories);
        when(bankConnectionRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(BankConnectionNotFoundException.class);

        verify(syncCategoriesUseCase).execute(credential);
        verify(transactionRepository, never()).deleteAll();
        verify(accountRepository, never()).deleteAll();
        verify(bankConnectionRepository, never()).save(any(BankConnection.class));
    }

    @Test
    void shouldSyncDataAndMarkConnectionUpdatedWhenGatewayCallsSucceed() {
        UserCredential credential = UserCredential.builder().clientId("id").clientSecret("secret").build();
        List<Category> categories = List.of(
                Category.builder().id("cat-1").description("Food").build(),
                Category.builder().id("cat-2").description("Salary").build()
        );

        BankConnection connection = BankConnection.builder()
                .id(1L)
                .itemId("item-1")
                .bankName("Bank A")
                .status(BankConnectionStatus.PENDING)
                .build();

        Account firstAccount = Account.builder()
                .id("acc-1")
                .type(AccountType.BANK)
                .subType(AccountSubType.CHECKING_ACCOUNT)
                .build();

        Account secondAccount = Account.builder()
                .id("acc-2")
                .type(AccountType.CREDIT)
                .subType(AccountSubType.CREDIT_CARD)
                .build();

        List<Account> accounts = List.of(firstAccount, secondAccount);

        Transaction tx1 = Transaction.builder()
                .id("tx-1")
                .accountId("acc-1")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("20.00"))
                .occurredAt(Instant.parse("2026-04-01T10:00:00Z"))
                .build();

        Transaction tx2 = Transaction.builder()
                .id("tx-2")
                .accountId("acc-2")
                .type(TransactionType.CREDIT)
                .amount(new BigDecimal("20.00"))
                .occurredAt(Instant.parse("2026-04-01T11:00:00Z"))
                .build();

        List<Transaction> fetchedTransactions = List.of(tx1, tx2);
        List<Transaction> classifiedTransactions = List.of(
                tx1.toBuilder().build(),
                tx2.toBuilder().build()
        );

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));
        when(syncCategoriesUseCase.execute(credential)).thenReturn(categories);
        when(bankConnectionRepository.findAll()).thenReturn(List.of(connection));
        when(financialGateway.fetchAccounts(credential, connection)).thenReturn(accounts);
        when(financialGateway.fetchTransactions(credential, "acc-1")).thenReturn(List.of(tx1));
        when(financialGateway.fetchTransactions(credential, "acc-2")).thenReturn(List.of(tx2));
        when(transactionClassificationUseCase.execute(fetchedTransactions, accounts, categories)).thenReturn(classifiedTransactions);
        when(bankConnectionRepository.save(any(BankConnection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SyncBankDataResponse response = useCase.execute();

        assertThat(response.categoriesSynced()).isEqualTo(2);
        assertThat(response.accountsSynced()).isEqualTo(2);
        assertThat(response.transactionsSynced()).isEqualTo(2);

        assertThat(connection.getStatus()).isEqualTo(BankConnectionStatus.UPDATED);
        assertThat(connection.getLastSyncAt()).isNotNull();

        verify(transactionRepository).deleteAll();
        verify(accountRepository).deleteAll();
        verify(accountRepository).saveAll(accounts);
        verify(transactionRepository).saveAll(classifiedTransactions);
        verify(bankConnectionRepository).save(connection);
    }

    @Test
    void shouldMarkConnectionAsErrorAndContinueWhenOneConnectionFails() {
        UserCredential credential = UserCredential.builder().clientId("id").clientSecret("secret").build();
        List<Category> categories = List.of(Category.builder().id("cat-1").description("General").build());

        BankConnection failingConnection = BankConnection.builder()
                .id(1L)
                .itemId("item-fail")
                .bankName("Broken Bank")
                .status(BankConnectionStatus.PENDING)
                .build();

        BankConnection successfulConnection = BankConnection.builder()
                .id(2L)
                .itemId("item-ok")
                .bankName("Healthy Bank")
                .status(BankConnectionStatus.PENDING)
                .build();

        Account successfulAccount = Account.builder()
                .id("acc-ok")
                .type(AccountType.BANK)
                .subType(AccountSubType.CHECKING_ACCOUNT)
                .build();

        Transaction successfulTransaction = Transaction.builder()
                .id("tx-ok")
                .accountId("acc-ok")
                .type(TransactionType.DEBIT)
                .amount(new BigDecimal("45.00"))
                .occurredAt(Instant.parse("2026-04-02T10:00:00Z"))
                .build();

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));
        when(syncCategoriesUseCase.execute(credential)).thenReturn(categories);
        when(bankConnectionRepository.findAll()).thenReturn(List.of(failingConnection, successfulConnection));

        when(financialGateway.fetchAccounts(credential, failingConnection)).thenThrow(new RuntimeException("Upstream failure"));
        when(financialGateway.fetchAccounts(credential, successfulConnection)).thenReturn(List.of(successfulAccount));
        when(financialGateway.fetchTransactions(credential, "acc-ok")).thenReturn(List.of(successfulTransaction));
        when(transactionClassificationUseCase.execute(anyList(), eq(List.of(successfulAccount)), eq(categories)))
                .thenReturn(List.of(successfulTransaction));
        when(bankConnectionRepository.save(any(BankConnection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SyncBankDataResponse response = useCase.execute();

        assertThat(response.categoriesSynced()).isEqualTo(1);
        assertThat(response.accountsSynced()).isEqualTo(1);
        assertThat(response.transactionsSynced()).isEqualTo(1);

        assertThat(failingConnection.getStatus()).isEqualTo(BankConnectionStatus.ERROR);
        assertThat(failingConnection.getLastSyncAt()).isNotNull();
        assertThat(successfulConnection.getStatus()).isEqualTo(BankConnectionStatus.UPDATED);
        assertThat(successfulConnection.getLastSyncAt()).isNotNull();

        verify(accountRepository).saveAll(List.of(successfulAccount));
        verify(transactionRepository).saveAll(List.of(successfulTransaction));
        verify(bankConnectionRepository, times(2)).save(any(BankConnection.class));
    }
}
