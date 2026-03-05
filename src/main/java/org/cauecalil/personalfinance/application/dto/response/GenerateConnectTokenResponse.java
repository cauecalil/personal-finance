package org.cauecalil.personalfinance.application.dto.response;

import lombok.Builder;

@Builder
public record GenerateConnectTokenResponse(
        String connectToken
) {}
