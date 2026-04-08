package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.dto.response.AccountResponse;
import com.cauecalil.personalfinance.domain.model.Account;
import com.cauecalil.personalfinance.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAccountsUseCase {
    private final AccountRepository accountRepository;

    public List<AccountResponse> execute() {
        List<Account> accounts = accountRepository.findAll();

        return accounts
                .stream()
                .map(AccountResponse::from)
                .toList();
    }
}
