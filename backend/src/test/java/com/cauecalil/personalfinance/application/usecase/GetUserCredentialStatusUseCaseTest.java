package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.GetUserCredentialStatusResponse;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserCredentialStatusUseCaseTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @InjectMocks
    private GetUserCredentialStatusUseCase useCase;

    @Test
    void shouldReturnConfiguredFalseWhenCredentialDoesNotExist() {
        when(userCredentialRepository.find()).thenReturn(Optional.empty());

        GetUserCredentialStatusResponse response = useCase.execute();

        assertThat(response.configured()).isFalse();
    }

    @Test
    void shouldReturnConfiguredTrueWhenCredentialExists() {
        UserCredential credential = UserCredential.builder()
                .clientId("11111111-1111-1111-1111-111111111111")
                .clientSecret("22222222-2222-2222-2222-222222222222")
                .build();

        when(userCredentialRepository.find()).thenReturn(Optional.of(credential));

        GetUserCredentialStatusResponse response = useCase.execute();

        assertThat(response.configured()).isTrue();
    }
}
