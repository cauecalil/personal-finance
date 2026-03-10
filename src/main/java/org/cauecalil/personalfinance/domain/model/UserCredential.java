package org.cauecalil.personalfinance.domain.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredential {
    private String clientId;
    private String clientSecret;
}