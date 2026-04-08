package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.AccountResponse;
import com.cauecalil.personalfinance.application.usecase.ListAccountsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final ListAccountsUseCase listAccountsUseCase;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> list() {
        List<AccountResponse> result = listAccountsUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
