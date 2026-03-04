package org.cauecalil.personalfinance.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredential {
    private Long id;
    private String clientId;
    private String clientSecret;
    private LocalDateTime lastSyncAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void markSynced() {
        this.lastSyncAt = LocalDateTime.now();
    }
}