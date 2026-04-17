package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.request.AddBankConnectionRequest;
import com.cauecalil.personalfinance.application.dto.response.BankConnectionResponse;
import com.cauecalil.personalfinance.application.usecase.AddBankConnectionUseCase;
import com.cauecalil.personalfinance.application.usecase.ListBankConnectionsUseCase;
import com.cauecalil.personalfinance.application.usecase.RemoveBankConnectionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-connections")
@RequiredArgsConstructor
@Tag(name = "Bank Connections", description = "Operations for registering, listing, and removing connected banks.")
public class BankConnectionController {
    private final ListBankConnectionsUseCase listBankConnectionsUseCase;
    private final AddBankConnectionUseCase addBankConnectionUseCase;
    private final RemoveBankConnectionUseCase removeBankConnectionUseCase;

    @GetMapping
    @Operation(
        summary = "List bank connections",
        description = "Return every configured bank connection and its latest synchronization status."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Bank connections were retrieved successfully.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BankConnectionResponse.class)))
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
    public ResponseEntity<List<BankConnectionResponse>> list() {
        List<BankConnectionResponse> result = listBankConnectionsUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    @Operation(
        summary = "Create a bank connection",
        description = "Register a new bank connection using a Pluggy item identifier so it can be included in future synchronizations."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Bank connection was created successfully.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankConnectionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed or business validation could not be satisfied.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "A bank connection for the same item already exists.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<BankConnectionResponse> add(@Valid @RequestBody AddBankConnectionRequest request) {
        BankConnectionResponse result = addBankConnectionUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remove a bank connection",
        description = "Delete a previously registered bank connection so it is no longer used in synchronization jobs."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Bank connection was removed successfully."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed or business validation could not be satisfied.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bank connection was not found.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<Void> remove(
        @Parameter(description = "Internal identifier of the bank connection to delete.", required = true, example = "12")
        @Valid @PathVariable Long id
    ) {
        removeBankConnectionUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
