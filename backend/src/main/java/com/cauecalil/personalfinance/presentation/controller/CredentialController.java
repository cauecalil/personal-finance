package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.request.SaveUserCredentialRequest;
import com.cauecalil.personalfinance.application.dto.response.GetUserCredentialStatusResponse;
import com.cauecalil.personalfinance.application.usecase.DeleteUserCredentialUseCase;
import com.cauecalil.personalfinance.application.usecase.GetUserCredentialStatusUseCase;
import com.cauecalil.personalfinance.application.usecase.SaveUserCredentialUseCase;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "Operations for storing, checking, and deleting gateway credentials.")
public class CredentialController {
    private final GetUserCredentialStatusUseCase getUserCredentialStatusUseCase;
    private final SaveUserCredentialUseCase saveUserCredentialUseCase;
    private final DeleteUserCredentialUseCase deleteUserCredentialUseCase;

    @GetMapping("/status")
    @Operation(
        summary = "Get credential configuration status",
        description = "Check whether API credentials are currently configured and available for synchronization operations."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Credential configuration status was returned successfully.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetUserCredentialStatusResponse.class))
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
    public ResponseEntity<GetUserCredentialStatusResponse> status() {
        GetUserCredentialStatusResponse response = getUserCredentialStatusUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @Operation(
        summary = "Save gateway credentials",
        description = "Store external provider credentials used for token generation and synchronization workflows. Existing credentials are replaced."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Credentials were saved successfully."
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
    public ResponseEntity<Object> save(@Valid @RequestBody SaveUserCredentialRequest request) {
        saveUserCredentialUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(
        summary = "Delete gateway credentials",
        description = "Remove stored credentials and disconnect all registered bank connections from the external provider."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Credentials and linked connections were deleted successfully."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed or credentials are not configured.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Credential resource was not found.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "502",
            description = "External provider communication failed while removing connections.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<Object> delete() {
        deleteUserCredentialUseCase.execute();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
