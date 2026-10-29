package com.example.shopping.products.api;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal priceAmount,
        String priceCurrency,
        boolean available
) {
}

