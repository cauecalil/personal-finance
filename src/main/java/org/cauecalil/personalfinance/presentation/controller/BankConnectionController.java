package org.cauecalil.personalfinance.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.request.AddBankConnectionRequest;
import org.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import org.cauecalil.personalfinance.application.usecase.AddBankConnectionUseCase;
import org.cauecalil.personalfinance.application.usecase.ListBankConnectionsUseCase;
import org.cauecalil.personalfinance.application.usecase.RemoveBankConnectionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-connections")
@RequiredArgsConstructor
public class BankConnectionController {
    private final ListBankConnectionsUseCase listBankConnectionsUseCase;
    private final AddBankConnectionUseCase addBankConnectionUseCase;
    private final RemoveBankConnectionUseCase removeBankConnectionUseCase;

    @GetMapping
    public ResponseEntity<List<BankConnectionResponse>> list() {
        List<BankConnectionResponse> result = listBankConnectionsUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<BankConnectionResponse> add(@Valid @RequestBody AddBankConnectionRequest request) {
        BankConnectionResponse result = addBankConnectionUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@Valid @RequestParam Long id) {
        removeBankConnectionUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
