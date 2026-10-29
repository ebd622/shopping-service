package com.example.shopping.contract;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.orders.api.OrderResponse;
import com.example.shopping.orders.api.OrdersController;
import com.example.shopping.orders.domain.OrderCancellationService;
import com.example.shopping.orders.domain.OrderCreationService;
import com.example.shopping.orders.domain.OrderQueryService;
import com.example.shopping.shared.error.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrdersController.class)
class OrdersLifecycleContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderCreationService orderCreationService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private OrderCancellationService orderCancellationService;

    @Test
    void shouldGetOrderById() throws Exception {
        UUID id = UUID.randomUUID();
        OrderResponse response = new OrderResponse(id, "CANCELED",
                List.of(new OrderResponse.Item(UUID.randomUUID(), 1, new BigDecimal("10.00"), new BigDecimal("10.00"))),
                new BigDecimal("10.00"), "USD", Instant.now(), Instant.now(), Instant.now());

        when(orderQueryService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderQueryService.getById(id)).thenThrow(new NotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        UUID id = UUID.randomUUID();
        OrderResponse response = new OrderResponse(id, "CANCELED", List.of(), BigDecimal.ZERO, "USD",
                Instant.now(), Instant.now(), Instant.now());
        when(orderCancellationService.cancel(id)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }
}

