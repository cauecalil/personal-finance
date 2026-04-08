package com.cauecalil.personalfinance.domain.model;

import com.cauecalil.personalfinance.domain.model.valueobject.BankConnectionStatus;
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
    private BankConnectionStatus status;
    private Instant lastSyncAt;

    public void markSynced(BankConnectionStatus status) {
        this.lastSyncAt = Instant.now();
        this.status = status;
    }
}
