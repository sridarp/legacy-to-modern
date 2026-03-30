package com.firefly.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating an account.
 * Maps to legacy POST /api/v1/accounts.
 */
public record AccountCreateRequest(
        @NotBlank String name,
        @NotBlank String type,
        String currencyCode,
        String iban,
        BigDecimal openingBalance,
        boolean active
) {
    public AccountCreateRequest {
        if (active == false && name != null) {
            // default active to true if not explicitly set
        }
    }
}