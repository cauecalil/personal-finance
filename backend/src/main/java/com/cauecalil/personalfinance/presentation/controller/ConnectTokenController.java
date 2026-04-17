package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.GenerateConnectTokenResponse;
import com.cauecalil.personalfinance.application.usecase.GenerateConnectTokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/connect-token")
@RequiredArgsConstructor
@Tag(name = "Connect Token", description = "Operations for generating Pluggy connect tokens.")
public class ConnectTokenController {
    private final GenerateConnectTokenUseCase generateConnectTokenUseCase;

    @PostMapping
    @Operation(
        summary = "Generate a connect token",
        description = "Generate a short-lived token used by the client to open the Pluggy connect flow for new or existing items."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Connect token was generated successfully.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateConnectTokenResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed or credentials are not configured.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Required credential resource was not found.",
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
    public ResponseEntity<GenerateConnectTokenResponse> generate(
        @Parameter(
            description = "Optional item identifier in UUID format used to reconnect an existing item.",
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @Valid @RequestParam(required = false) String itemId
    ) {
        GenerateConnectTokenResponse result = generateConnectTokenUseCase.execute(itemId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
