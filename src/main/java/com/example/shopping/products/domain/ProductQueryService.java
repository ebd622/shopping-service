package com.example.shopping.products.domain;

import com.example.shopping.products.api.ProductResponse;
import com.example.shopping.products.infra.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(product -> new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPriceAmount(),
                product.getPriceCurrency(),
                product.isAvailable()
        )).toList();
    }
}

