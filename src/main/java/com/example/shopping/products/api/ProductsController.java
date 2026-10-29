package com.example.shopping.products.api;

import com.example.shopping.products.domain.ProductQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {

    private final ProductQueryService productQueryService;

    public ProductsController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        return productQueryService.findAll();
    }
}

