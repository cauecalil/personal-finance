package org.cauecalil.personalfinance.application.dto.request;

import jakarta.validation.constraints.Pattern;

public record SaveUserCredentialRequest(
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Client ID must be a valid UUID"
        )
        String clientId,

        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Client secret must be a valid UUID"
        )
        String clientSecret
) {}
