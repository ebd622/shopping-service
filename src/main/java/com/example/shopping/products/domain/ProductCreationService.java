package com.example.shopping.products.domain;

import com.example.shopping.products.api.CreateProductRequest;
import com.example.shopping.products.api.ProductResponse;
import com.example.shopping.products.infra.ProductEntity;
import com.example.shopping.products.infra.ProductRepository;
import com.example.shopping.shared.error.ConflictException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCreationService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final ProductRepository productRepository;

    public ProductCreationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String normalizedSku = request.sku().trim();
        if (productRepository.existsBySku(normalizedSku)) {
            throw new ConflictException("Product already exists for sku: " + normalizedSku);
        }

        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID());
        product.setSku(normalizedSku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPriceAmount(request.price());
        product.setPriceCurrency(normalizeCurrency(request.currency()));
        product.setAvailable(true);
        product.setUpdatedAt(Instant.now());

        try {
            ProductEntity saved = productRepository.save(product);
            return new ProductResponse(
                    saved.getId(),
                    saved.getSku(),
                    saved.getName(),
                    saved.getDescription(),
                    saved.getPriceAmount(),
                    saved.getPriceCurrency(),
                    saved.isAvailable()
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Product already exists for sku: " + normalizedSku);
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}

