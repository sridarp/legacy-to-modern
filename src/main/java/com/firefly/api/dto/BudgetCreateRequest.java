package com.firefly.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a budget.
 */
public record BudgetCreateRequest(
        @NotBlank String name
) {}