package com.firefly.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO for creating a transaction.
 * Maps to legacy POST /api/v1/transactions.
 */
public record TransactionCreateRequest(
        @NotBlank String type,
        @NotBlank String description,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currencyCode,
        @NotNull Long sourceAccountId,
        @NotNull Long destinationAccountId,
        @NotBlank String date
) {}