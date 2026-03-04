package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetUserCredentialStatusResponse(
     boolean configured,
     LocalDateTime lastSyncAt
) {}
