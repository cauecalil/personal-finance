package com.cauecalil.personalfinance.domain.repository;

import com.cauecalil.personalfinance.domain.model.UserCredential;

import java.util.Optional;

public interface UserCredentialRepository {
    UserCredential save(UserCredential credential);
    Optional<UserCredential> find();
    void delete();
}
