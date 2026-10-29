package com.example.shopping.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.integration.support.PostgresContainerSupport;
import com.example.shopping.orders.domain.OrderStatus;
import com.example.shopping.orders.infra.OrderEntity;
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
class OrdersLifecycleIntegrationTest extends PostgresContainerSupport {

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
        product.setSku("SKU-300");
        product.setName("Monitor");
        product.setDescription("4K monitor");
        product.setPriceAmount(new BigDecimal("100.00"));
        product.setPriceCurrency("USD");
        product.setAvailable(true);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    @Test
    void shouldCancelAndRetrieveCanceledOrder() throws Exception {
        String payload = "{\"items\":[{\"productId\":\"" + productId + "\",\"quantity\":1}]}";

        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "idemp-l1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = com.jayway.jsonpath.JsonPath.read(body, "$.id");

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturnConflictWhenCompletedOrderIsCanceled() throws Exception {
        OrderEntity order = new OrderEntity();
        UUID id = UUID.randomUUID();
        order.setId(id);
        order.setStatus(OrderStatus.COMPLETED);
        order.setCurrency("USD");
        order.setTotalAmount(new BigDecimal("1.00"));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void shouldReturnConflictWhenShippedOrderIsCanceled() throws Exception {
        OrderEntity order = new OrderEntity();
        UUID id = UUID.randomUUID();
        order.setId(id);
        order.setStatus(OrderStatus.SHIPPED);
        order.setCurrency("USD");
        order.setTotalAmount(new BigDecimal("1.00"));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void shouldReturnValidationErrorForMalformedOrderId() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", "not-a-uuid"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}

