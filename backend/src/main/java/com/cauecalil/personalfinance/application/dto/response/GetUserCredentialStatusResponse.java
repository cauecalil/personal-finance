package com.cauecalil.personalfinance.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Indicates whether external financial gateway credentials are currently configured.")
public record GetUserCredentialStatusResponse(
     @Schema(description = "True when credentials are stored and available for synchronization actions.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
     boolean configured
) {}
