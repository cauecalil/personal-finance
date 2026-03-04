package org.cauecalil.personalfinance.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cauecalil.personalfinance.application.dto.request.SaveUserCredentialRequest;
import org.cauecalil.personalfinance.application.dto.response.GetUserCredentialStatusResponse;
import org.cauecalil.personalfinance.application.usecase.DeleteUserCredentialUseCase;
import org.cauecalil.personalfinance.application.usecase.GetUserCredentialStatusUseCase;
import org.cauecalil.personalfinance.application.usecase.SaveUserCredentialUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialController {
    private final GetUserCredentialStatusUseCase getUserCredentialStatusUseCase;
    private final SaveUserCredentialUseCase saveUserCredentialUseCase;
    private final DeleteUserCredentialUseCase deleteUserCredentialUseCase;

    @GetMapping("/status")
    public ResponseEntity<GetUserCredentialStatusResponse> status() {
        GetUserCredentialStatusResponse response = getUserCredentialStatusUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody SaveUserCredentialRequest request) {
        saveUserCredentialUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Object> delete() {
        deleteUserCredentialUseCase.execute();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
