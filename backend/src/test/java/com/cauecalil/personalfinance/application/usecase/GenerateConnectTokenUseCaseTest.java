package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GenerateConnectTokenResponse;
import com.cauecalil.personalfinance.application.exception.UserCredentialNotFoundException;
import com.cauecalil.personalfinance.application.port.FinancialGateway;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateConnectTokenUseCaseTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private FinancialGateway financialGateway;

    @InjectMocks
    private GenerateConnectTokenUseCase useCase;

    @Test
    void shouldReturnConnectTokenWhenCredentialExists() {
        String itemId = "item-1";
        UserCredential credential = UserCredential.builder()
                .clientId("11111111-1111-1111-1111-111111111111")
                .clientSecret("22222222-2222-2222-2222-222222222222")
                .build();

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));
        when(financialGateway.createConnectionToken(credential, itemId)).thenReturn("connect-token");

        GenerateConnectTokenResponse response = useCase.execute(itemId);

        assertThat(response.connectToken()).isEqualTo("connect-token");
        verify(financialGateway).createConnectionToken(credential, itemId);
    }

    @Test
    void shouldThrowUserCredentialNotFoundExceptionWhenCredentialDoesNotExist() {
        when(userCredentialRepository.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("item-1"))
                .isInstanceOf(UserCredentialNotFoundException.class);

        verifyNoInteractions(financialGateway);
    }
}
