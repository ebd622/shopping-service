package com.example.shopping.products.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "sku is required")
        String sku,
        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be <= 200")
        String name,
        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be >= 0.01")
        BigDecimal price,
        @Size(max = 2000, message = "description must be <= 2000")
        String description,
        @Size(min = 3, max = 3, message = "currency must be ISO-4217")
        String currency
) {
}

