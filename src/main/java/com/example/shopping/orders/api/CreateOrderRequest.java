package com.example.shopping.orders.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotEmpty(message = "items must not be empty")
        List<@Valid Item> items
) {
    public record Item(
            @NotNull(message = "productId is required")
            UUID productId,
            @Min(value = 1, message = "quantity must be >= 1")
            int quantity
    ) {
    }
}

