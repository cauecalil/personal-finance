package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.exception.BankConnectionNotFoundException;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveBankConnectionUseCaseTest {

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @InjectMocks
    private RemoveBankConnectionUseCase useCase;

    @Test
    void shouldDeleteBankConnectionWhenIdExists() {
        Long id = 99L;
        when(bankConnectionRepository.existsById(id)).thenReturn(true);

        useCase.execute(id);

        verify(bankConnectionRepository).deleteById(id);
    }

    @Test
    void shouldThrowBankConnectionNotFoundExceptionWhenIdDoesNotExist() {
        Long id = 99L;
        when(bankConnectionRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(id))
                .isInstanceOf(BankConnectionNotFoundException.class);

        verify(bankConnectionRepository, never()).deleteById(id);
    }
}
