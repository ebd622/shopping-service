package com.example.shopping.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.orders.api.OrderResponse;
import com.example.shopping.orders.api.OrdersController;
import com.example.shopping.orders.domain.OrderCancellationService;
import com.example.shopping.orders.domain.OrderCreationService;
import com.example.shopping.orders.domain.OrderQueryService;
import com.example.shopping.shared.error.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersController.class)
class OrdersCreateContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderCreationService orderCreationService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private OrderCancellationService orderCancellationService;

    @Test
    void shouldCreateOrder() throws Exception {
        OrderResponse response = new OrderResponse(UUID.randomUUID(), "CREATED",
                List.of(new OrderResponse.Item(UUID.randomUUID(), 1, new BigDecimal("10.00"), new BigDecimal("10.00"))),
                new BigDecimal("10.00"), "USD", Instant.now(), Instant.now(), null);
        when(orderCreationService.createOrder(eq("key-1"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":\"11111111-1111-1111-1111-111111111111\",\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturnConflict() throws Exception {
        when(orderCreationService.createOrder(eq("key-1"), any()))
                .thenThrow(new ConflictException("Idempotency key reused with a different request payload"));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":\"11111111-1111-1111-1111-111111111111\",\"quantity\":1}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}

