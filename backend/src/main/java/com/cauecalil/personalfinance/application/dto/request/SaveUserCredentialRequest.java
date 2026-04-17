package com.cauecalil.personalfinance.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload used to save Pluggy API credentials for synchronization operations.")
public record SaveUserCredentialRequest(
        @Schema(
                description = "Pluggy client identifier in UUID format, e.g. 550e8400-e29b-41d4-a716-446655440000.",
                example = "550e8400-e29b-41d4-a716-446655440000",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Client ID must be a valid UUID"
        )
        String clientId,

        @Schema(
                description = "Pluggy client secret in UUID format, e.g. c9b0f9f4-c2ab-4d7f-8dd5-2f4f9a3a6a2f.",
                example = "c9b0f9f4-c2ab-4d7f-8dd5-2f4f9a3a6a2f",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Client secret must be a valid UUID"
        )
        String clientSecret
) {}
