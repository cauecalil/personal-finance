package org.cauecalil.personalfinance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_connections")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BankConnectionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemId;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime lastSyncAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
