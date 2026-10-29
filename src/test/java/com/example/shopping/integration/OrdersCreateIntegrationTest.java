package com.example.shopping.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.integration.support.PostgresContainerSupport;
import com.example.shopping.orders.infra.OrderIdempotencyRepository;
import com.example.shopping.orders.infra.OrderItemRepository;
import com.example.shopping.orders.infra.OrderRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrdersCreateIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderIdempotencyRepository idempotencyRepository;

    private UUID productId;

    @BeforeEach
    void setup() {
        orderItemRepository.deleteAll();
        idempotencyRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();

        ProductEntity product = new ProductEntity();
        productId = UUID.randomUUID();
        product.setId(productId);
        product.setSku("SKU-200");
        product.setName("Mouse");
        product.setDescription("Wireless mouse");
        product.setPriceAmount(new BigDecimal("30.00"));
        product.setPriceCurrency("USD");
        product.setAvailable(true);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    @Test
    void shouldCreateAndDeduplicateByIdempotencyKey() throws Exception {
        String payload = "{\"items\":[{\"productId\":\"" + productId + "\",\"quantity\":2}]}";

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "idemp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "idemp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        org.assertj.core.api.Assertions.assertThat(orderRepository.findAll()).hasSize(1);
    }
}

