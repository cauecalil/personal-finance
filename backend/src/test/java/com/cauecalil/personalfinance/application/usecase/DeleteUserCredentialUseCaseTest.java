package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import com.cauecalil.personalfinance.application.port.FinancialGateway;
import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserCredentialUseCaseTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @Mock
    private FinancialGateway financialGateway;

    @InjectMocks
    private DeleteUserCredentialUseCase useCase;

    @Test
    void shouldRemoveConnectionsAndDeleteCredentialWhenCredentialExists() {
        UserCredential credential = UserCredential.builder()
                .clientId("11111111-1111-1111-1111-111111111111")
                .clientSecret("22222222-2222-2222-2222-222222222222")
                .build();

        BankConnection first = BankConnection.builder().id(1L).itemId("item-1").bankName("Bank A").build();
        BankConnection second = BankConnection.builder().id(2L).itemId("item-2").bankName("Bank B").build();

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));
        when(bankConnectionRepository.findAll()).thenReturn(List.of(first, second));

        useCase.execute();

        verify(financialGateway).removeConnection(credential, "item-1");
        verify(financialGateway).removeConnection(credential, "item-2");
        verify(bankConnectionRepository).deleteById(1L);
        verify(bankConnectionRepository).deleteById(2L);
        verify(userCredentialRepository).delete();
    }

    @Test
    void shouldDeleteCredentialWithoutRemovingConnectionsWhenNoConnectionsExist() {
        UserCredential credential = UserCredential.builder()
                .clientId("11111111-1111-1111-1111-111111111111")
                .clientSecret("22222222-2222-2222-2222-222222222222")
                .build();

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));
        when(bankConnectionRepository.findAll()).thenReturn(List.of());

        useCase.execute();

        verifyNoInteractions(financialGateway);
        verify(bankConnectionRepository, never()).deleteById(anyLong());
        verify(userCredentialRepository).delete();
    }

    @Test
    void shouldThrowUserCredentialNotFoundExceptionWhenCredentialDoesNotExist() {
        when(userCredentialRepository.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute())
                .isInstanceOf(UserCredentialNotFoundException.class);

        verify(bankConnectionRepository, never()).findAll();
        verifyNoInteractions(financialGateway);
        verify(userCredentialRepository, never()).delete();
    }
}
