package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.GenerateConnectTokenResponse;
import com.cauecalil.personalfinance.application.usecase.GenerateConnectTokenUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/connect-token")
@RequiredArgsConstructor
public class ConnectTokenController {
    private final GenerateConnectTokenUseCase generateConnectTokenUseCase;

    @PostMapping
    public ResponseEntity<GenerateConnectTokenResponse> generate(@Valid @RequestParam(required = false) String itemId) {
        GenerateConnectTokenResponse result = generateConnectTokenUseCase.execute(itemId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
