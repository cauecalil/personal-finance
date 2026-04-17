package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Response payload containing a short-lived connect token used to initiate the Pluggy connection flow.")
public record GenerateConnectTokenResponse(
        @Schema(description = "Ephemeral token consumed by the client-side Pluggy widget to connect or reconnect an account.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.connect-token-sample", requiredMode = Schema.RequiredMode.REQUIRED)
        String connectToken
) {}
