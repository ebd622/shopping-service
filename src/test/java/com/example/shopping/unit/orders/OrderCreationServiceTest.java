package com.example.shopping.unit.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shopping.orders.api.CreateOrderRequest;
import com.example.shopping.orders.domain.OrderCreationService;
import com.example.shopping.orders.domain.OrderMapper;
import com.example.shopping.orders.domain.OrderStatus;
import com.example.shopping.orders.infra.OrderEntity;
import com.example.shopping.orders.infra.OrderIdempotencyEntity;
import com.example.shopping.orders.infra.OrderIdempotencyRepository;
import com.example.shopping.orders.infra.OrderRepository;
import com.example.shopping.products.infra.ProductEntity;
import com.example.shopping.products.infra.ProductRepository;
import com.example.shopping.shared.error.ConflictException;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OrderCreationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderIdempotencyRepository idempotencyRepository;

    private OrderCreationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderCreationService(orderRepository, productRepository, idempotencyRepository,
                new OrderMapper(), new ObjectMapper());
    }

    @Test
    void shouldCreateOrder() {
        UUID productId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(List.of(new CreateOrderRequest.Item(productId, 2)));
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setSku("SKU-1");
        product.setName("Product");
        product.setPriceAmount(new BigDecimal("5.00"));
        product.setPriceCurrency("USD");
        product.setAvailable(true);
        product.setUpdatedAt(Instant.now());

        when(idempotencyRepository.findById("k1")).thenReturn(Optional.empty());
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(product));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.createOrder("k1", request);

        assertThat(response.status()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(response.totalAmount()).isEqualByComparingTo("10.00");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void shouldRejectReusedKeyWithDifferentPayload() {
        OrderIdempotencyEntity existing = new OrderIdempotencyEntity();
        existing.setIdempotencyKey("k1");
        existing.setRequestHash("old");
        existing.setOrderId(UUID.randomUUID());
        existing.setCreatedAt(Instant.now());
        when(idempotencyRepository.findById("k1")).thenReturn(Optional.of(existing));

        CreateOrderRequest request = new CreateOrderRequest(List.of(new CreateOrderRequest.Item(UUID.randomUUID(), 1)));
        assertThatThrownBy(() -> service.createOrder("k1", request)).isInstanceOf(ConflictException.class);
    }
}

