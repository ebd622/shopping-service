package com.example.shopping.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.integration.support.PostgresContainerSupport;
import com.example.shopping.products.infra.ProductEntity;
import com.example.shopping.products.infra.ProductRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductsIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID());
        product.setSku("SKU-100");
        product.setName("Keyboard");
        product.setDescription("Mechanical keyboard");
        product.setPriceAmount(new BigDecimal("25.00"));
        product.setPriceCurrency("USD");
        product.setAvailable(true);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    @Test
    void shouldListProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-100"));
    }
}

