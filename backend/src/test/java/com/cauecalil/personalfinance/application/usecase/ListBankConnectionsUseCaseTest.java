package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBankConnectionsUseCaseTest {

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @InjectMocks
    private ListBankConnectionsUseCase useCase;

    @Test
    void shouldMapBankConnectionsWhenRepositoryReturnsData() {
        Instant syncedAt = Instant.parse("2026-04-10T12:00:00Z");

        BankConnection first = BankConnection.builder()
                .id(1L)
                .itemId("item-1")
                .bankName("Bank A")
                .status(BankConnectionStatus.UPDATED)
                .lastSyncAt(syncedAt)
                .build();

        BankConnection second = BankConnection.builder()
                .id(2L)
                .itemId("item-2")
                .bankName("Bank B")
                .status(BankConnectionStatus.PENDING)
                .lastSyncAt(null)
                .build();

        when(bankConnectionRepository.findAll()).thenReturn(List.of(first, second));

        List<BankConnectionResponse> response = useCase.execute();

        assertThat(response)
                .extracting(
                        BankConnectionResponse::id,
                        BankConnectionResponse::itemId,
                        BankConnectionResponse::bankName,
                        BankConnectionResponse::status,
                        BankConnectionResponse::lastSyncAt
                )
                .containsExactly(
                        tuple(1L, "item-1", "Bank A", BankConnectionStatus.UPDATED, syncedAt),
                        tuple(2L, "item-2", "Bank B", BankConnectionStatus.PENDING, null)
                );

        verify(bankConnectionRepository).findAll();
    }
}
