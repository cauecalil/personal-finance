package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.request.SaveUserCredentialRequest;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveUserCredentialUseCaseTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @InjectMocks
    private SaveUserCredentialUseCase useCase;

    @Test
    void shouldSaveCredentialWhenRequestIsValid() {
        SaveUserCredentialRequest request = new SaveUserCredentialRequest(
                "11111111-1111-1111-1111-111111111111",
                "22222222-2222-2222-2222-222222222222"
        );

        useCase.execute(request);

        ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
        verify(userCredentialRepository).save(captor.capture());

        UserCredential saved = captor.getValue();
        assertThat(saved.getClientId()).isEqualTo(request.clientId());
        assertThat(saved.getClientSecret()).isEqualTo(request.clientSecret());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }
}
