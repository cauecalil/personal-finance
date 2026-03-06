package org.cauecalil.personalfinance.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredential {
    private Long id;
    private String clientId;
    private String clientSecret;
    private Instant lastSyncAt;

    public void markSynced() {
        this.lastSyncAt = Instant.now();
    }
}