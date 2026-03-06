package org.cauecalil.personalfinance.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BankConnection {
    private Long id;
    private String itemId;
    private String bankName;
    private String status;
    private Instant lastSyncAt;

    public void markSynced(String status) {
        this.lastSyncAt = Instant.now();
        this.status = status;
    }
}
