package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.request.AddBankConnectionRequest;
import com.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import com.cauecalil.personalfinance.application.exception.BankConnectionAlreadyExistsException;
import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddBankConnectionUseCaseTest {

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @InjectMocks
    private AddBankConnectionUseCase useCase;

    @Test
    void shouldSavePendingConnectionWhenItemDoesNotExist() {
        AddBankConnectionRequest request = new AddBankConnectionRequest(
                "11111111-1111-1111-1111-111111111111",
                "My Bank"
        );

        Instant lastSyncAt = Instant.parse("2026-04-01T12:00:00Z");
        BankConnection savedConnection = BankConnection.builder()
                .id(10L)
                .itemId(request.itemId())
                .bankName(request.bankName())
                .status(BankConnectionStatus.PENDING)
                .lastSyncAt(lastSyncAt)
                .build();

        when(bankConnectionRepository.existsByItemId(request.itemId())).thenReturn(false);
        when(bankConnectionRepository.save(any(BankConnection.class))).thenReturn(savedConnection);

        BankConnectionResponse response = useCase.execute(request);

        ArgumentCaptor<BankConnection> captor = ArgumentCaptor.forClass(BankConnection.class);
        verify(bankConnectionRepository).save(captor.capture());

        BankConnection toSave = captor.getValue();
        assertThat(toSave.getItemId()).isEqualTo(request.itemId());
        assertThat(toSave.getBankName()).isEqualTo(request.bankName());
        assertThat(toSave.getStatus()).isEqualTo(BankConnectionStatus.PENDING);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.itemId()).isEqualTo(request.itemId());
        assertThat(response.bankName()).isEqualTo(request.bankName());
        assertThat(response.status()).isEqualTo(BankConnectionStatus.PENDING);
        assertThat(response.lastSyncAt()).isEqualTo(lastSyncAt);
    }

    @Test
    void shouldThrowBankConnectionAlreadyExistsExceptionWhenItemAlreadyExists() {
        AddBankConnectionRequest request = new AddBankConnectionRequest(
                "11111111-1111-1111-1111-111111111111",
                "My Bank"
        );

        when(bankConnectionRepository.existsByItemId(request.itemId())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BankConnectionAlreadyExistsException.class);

        verify(bankConnectionRepository, never()).save(any(BankConnection.class));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verify(bankConnectionRepository, never()).save(any(BankConnection.class));
    }
}
