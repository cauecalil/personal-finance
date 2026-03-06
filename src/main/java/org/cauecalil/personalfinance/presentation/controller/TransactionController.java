package org.cauecalil.personalfinance.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.usecase.ListTransactionsUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final ListTransactionsUseCase listTransactionsUseCase;

    @GetMapping("/{accountId}")
    public ResponseEntity<List<TransactionResponse>> list(
            @PathVariable String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<TransactionResponse> result = listTransactionsUseCase.execute(accountId, from, to);
        return ResponseEntity.ok().body(result);
    }
}
