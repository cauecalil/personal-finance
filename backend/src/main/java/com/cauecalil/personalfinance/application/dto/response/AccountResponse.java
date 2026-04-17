package com.cauecalil.personalfinance.application.dto.response;

import com.cauecalil.personalfinance.domain.model.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Represents a synchronized financial account available for querying transactions and dashboard data.")
public record AccountResponse(
    @Schema(description = "Unique account identifier from the external financial provider.", example = "f4e3a2b1-6d5c-4b3a-9f87-1d2c3b4a5e6f", requiredMode = Schema.RequiredMode.REQUIRED)
        String id,
    @Schema(description = "Human-readable account name shown in the UI.", example = "NuConta Principal", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
    @Schema(description = "Account type. Possible values: BANK, CREDIT.", example = "BANK", requiredMode = Schema.RequiredMode.REQUIRED)
        String type,
    @Schema(description = "Account subtype. Possible values include SAVINGS_ACCOUNT, CHECKING_ACCOUNT, CREDIT_CARD.", example = "CHECKING_ACCOUNT", requiredMode = Schema.RequiredMode.REQUIRED)
        String subtype,
    @Schema(description = "Current account balance in the account currency.", example = "2540.75", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal balance,
    @Schema(description = "ISO 4217 currency code for the account balance.", example = "BRL", requiredMode = Schema.RequiredMode.REQUIRED)
        String currency
) {
    public static AccountResponse from(Account account) {
        String name = account.getMarketingName() == null ? account.getName() : account.getMarketingName();

        return AccountResponse.builder()
                .id(account.getId())
                .name(name)
                .type(account.getType().name())
                .subtype(account.getSubType().name())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }
}
