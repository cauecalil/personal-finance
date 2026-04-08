package com.cauecalil.personalfinance.infrastructure.persistence.adapter;

import com.cauecalil.personalfinance.domain.model.BankConnection;
import com.cauecalil.personalfinance.domain.repository.BankConnectionRepository;
import com.cauecalil.personalfinance.infrastructure.persistence.entity.BankConnectionJpaEntity;
import com.cauecalil.personalfinance.infrastructure.persistence.mapper.BankConnectionMapper;
import com.cauecalil.personalfinance.infrastructure.persistence.repository.BankConnectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public List<BankConnection> findAll() {
        return bankConnectionJpaRepository.findAll().stream().map(BankConnectionMapper::toDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return bankConnectionJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByItemId(String itemId) {
        return bankConnectionJpaRepository.existsByItemId(itemId);
    }

    @Override
    public void deleteById(Long id) {
        bankConnectionJpaRepository.deleteById(id);
    }
}
