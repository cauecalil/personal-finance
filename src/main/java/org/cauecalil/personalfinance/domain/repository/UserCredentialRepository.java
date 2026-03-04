package org.cauecalil.personalfinance.domain.repository;

import org.cauecalil.personalfinance.domain.model.UserCredential;

import java.util.Optional;

public interface UserCredentialRepository {
    UserCredential save(UserCredential credential);
    Optional<UserCredential> findById(Long id);
    void delete(Long id);
}
