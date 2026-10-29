package com.example.shopping.products.api;

import com.example.shopping.products.domain.ProductQueryService;
import com.example.shopping.products.domain.ProductCreationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {

    private final ProductQueryService productQueryService;
    private final ProductCreationService productCreationService;

    public ProductsController(ProductQueryService productQueryService,
                              ProductCreationService productCreationService) {
        this.productQueryService = productQueryService;
        this.productCreationService = productCreationService;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        return productQueryService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productCreationService.create(request);
    }
}

