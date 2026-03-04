package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.UserCredential;
import org.cauecalil.personalfinance.domain.repository.UserCredentialRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.UserCredentialJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.UserCredentialMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.UserCredentialJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCredentialRepositoryAdapter implements UserCredentialRepository {
    private final UserCredentialJpaRepository userCredentialJpaRepository;

    @Override
    public UserCredential save(UserCredential credential) {
        UserCredentialJpaEntity entity = UserCredentialMapper.toEntity(credential);
        UserCredentialJpaEntity saved = userCredentialJpaRepository.save(entity);
        return UserCredentialMapper.toDomain(saved);
    }

    @Override
    public Optional<UserCredential> findFirst() {
        return userCredentialJpaRepository.findFirstByOrderByIdAsc().map(UserCredentialMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        userCredentialJpaRepository.deleteById(id);
    }
}
