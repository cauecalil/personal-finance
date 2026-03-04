package org.cauecalil.personalfinance.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BankConnection {
    private String id;
    private String itemId;
    private String bankName;
    private String status;

    private LocalDateTime lastSyncAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void markSynced(String status) {
        this.lastSyncAt = LocalDateTime.now();
        this.status = status;
    }
}
