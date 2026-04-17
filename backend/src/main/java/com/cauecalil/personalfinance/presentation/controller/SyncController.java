package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.SyncBankDataResponse;
import com.cauecalil.personalfinance.application.usecase.SyncBankDataUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Operations for triggering end-to-end synchronization of banking data.")
public class SyncController {
    private final SyncBankDataUseCase syncBankDataUseCase;

    @PostMapping
    @Operation(
        summary = "Start bank data synchronization",
        description = "Trigger synchronization of categories, accounts, and transactions for all registered bank connections and persist the latest snapshot."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Synchronization completed and summary counters were returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SyncBankDataResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed or required setup data is missing.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Required resource for synchronization was not found.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "502",
            description = "External provider communication failed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<SyncBankDataResponse> sync() {
        SyncBankDataResponse result = syncBankDataUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
