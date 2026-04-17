package com.cauecalil.personalfinance.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload used to register a new bank connection by Pluggy item identifier.")
public record AddBankConnectionRequest(
        @Schema(
                description = "Pluggy item identifier in UUID format, e.g. 550e8400-e29b-41d4-a716-446655440000.",
                example = "550e8400-e29b-41d4-a716-446655440000",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Item ID must be a valid UUID"
        )
        String itemId,

        @Schema(
                description = "Display name of the bank associated with the connection.",
                example = "Nubank",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String bankName
) {}
