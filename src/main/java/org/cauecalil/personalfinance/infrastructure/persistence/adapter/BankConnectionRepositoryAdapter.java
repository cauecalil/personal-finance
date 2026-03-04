package org.cauecalil.personalfinance.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.domain.model.BankConnection;
import org.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import org.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;
import org.cauecalil.personalfinance.infrastructure.persistence.mapper.BankConnectionMapper;
import org.cauecalil.personalfinance.infrastructure.persistence.repository.BankConnectionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BankConnectionRepositoryAdapter implements BankConnectionRepository {
    private final BankConnectionJpaRepository bankConnectionJpaRepository;

    @Override
    public BankConnection save(BankConnection bankConnection) {
        BankConnectionJpaEntity entity = BankConnectionMapper.toEntity(bankConnection);
        BankConnectionJpaEntity saved = bankConnectionJpaRepository.save(entity);
        return BankConnectionMapper.toDomain(saved);
    }

    @Override
    public Optional<BankConnection> findById(Long id) {
        return bankConnectionJpaRepository.findById(id).map(BankConnectionMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        bankConnectionJpaRepository.deleteById(id);
    }
}
