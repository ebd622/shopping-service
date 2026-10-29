package com.example.shopping.unit.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.shopping.orders.domain.OrderCancellationService;
import com.example.shopping.orders.domain.OrderMapper;
import com.example.shopping.orders.domain.OrderStatus;
import com.example.shopping.orders.infra.OrderEntity;
import com.example.shopping.orders.infra.OrderRepository;
import com.example.shopping.shared.error.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OrderLifecycleServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderCancellationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderCancellationService(orderRepository, new OrderMapper());
    }

    @Test
    void shouldReturnNoOpForAlreadyCanceled() {
        OrderEntity order = baseOrder(OrderStatus.CANCELED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        var response = service.cancel(order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED.name());
    }

    @Test
    void shouldFailForCompletedOrder() {
        OrderEntity order = baseOrder(OrderStatus.COMPLETED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel(order.getId())).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldFailForShippedOrder() {
        OrderEntity order = baseOrder(OrderStatus.SHIPPED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel(order.getId())).isInstanceOf(ConflictException.class);
    }

    private OrderEntity baseOrder(OrderStatus status) {
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID());
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.ONE);
        order.setCurrency("USD");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        return order;
    }
}


