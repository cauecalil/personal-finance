package org.cauecalil.personalfinance.application.usecase;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.AccountResponse;
import org.cauecalil.personalfinance.domain.model.Account;
import org.cauecalil.personalfinance.domain.repository.AccountRepository;
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
