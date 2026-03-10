package org.cauecalil.personalfinance.infrastructure.keystore;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class KeyStoreUserCredentialRepository implements UserCredentialRepository {
    private final KeyStoreCredentialStore keyStore;

    @Override
    public Optional<UserCredential> find() {
        return keyStore.load().map(credentials ->
                UserCredential.builder()
                        .clientId(credentials[0])
                        .clientSecret(credentials[1])
                        .build()
        );
    }

    @Override
    public UserCredential save(UserCredential credential) {
        keyStore.save(credential.getClientId(), credential.getClientSecret());
        return credential;
    }

    @Override
    public void delete() {
        keyStore.delete();
    }
}