package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record GetUserCredentialStatusResponse(
     boolean configured,
     Instant lastSyncAt
) {}
