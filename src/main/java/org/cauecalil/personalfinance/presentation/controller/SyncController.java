package org.cauecalil.personalfinance.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.response.SyncBankDataResponse;
import org.cauecalil.personalfinance.application.usecase.SyncBankDataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {
    private final SyncBankDataUseCase syncBankDataUseCase;

    @PostMapping
    public ResponseEntity<SyncBankDataResponse> sync() {
        SyncBankDataResponse result = syncBankDataUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
