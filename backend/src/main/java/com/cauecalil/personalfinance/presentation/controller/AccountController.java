package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.AccountResponse;
import com.cauecalil.personalfinance.application.usecase.ListAccountsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Operations for listing synchronized financial accounts.")
public class AccountController {
    private final ListAccountsUseCase listAccountsUseCase;

    @GetMapping
    @Operation(
        summary = "List synchronized accounts",
        description = "Retrieve all accounts currently stored in the local database after synchronization. This endpoint does not trigger a sync operation."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Accounts were retrieved successfully.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<List<AccountResponse>> list() {
        List<AccountResponse> result = listAccountsUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
