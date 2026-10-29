package com.example.shopping.orders.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String status,
        List<Item> items,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        Instant updatedAt,
        Instant canceledAt
) {
    public record Item(
            UUID productId,
            int quantity,
            BigDecimal unitPriceAmount,
            BigDecimal lineAmount
    ) {
    }
}

