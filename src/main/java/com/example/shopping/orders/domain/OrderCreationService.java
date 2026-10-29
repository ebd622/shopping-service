package com.example.shopping.orders.domain;

import com.example.shopping.orders.api.CreateOrderRequest;
import com.example.shopping.orders.api.OrderResponse;
import com.example.shopping.orders.infra.OrderEntity;
import com.example.shopping.orders.infra.OrderIdempotencyEntity;
import com.example.shopping.orders.infra.OrderIdempotencyRepository;
import com.example.shopping.orders.infra.OrderItemEntity;
import com.example.shopping.orders.infra.OrderRepository;
import com.example.shopping.products.infra.ProductEntity;
import com.example.shopping.products.infra.ProductRepository;
import com.example.shopping.shared.error.ConflictException;
import com.example.shopping.shared.error.NotFoundException;
import com.example.shopping.shared.error.ServiceUnavailableException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderIdempotencyRepository idempotencyRepository;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    public OrderCreationService(OrderRepository orderRepository,
                                ProductRepository productRepository,
                                OrderIdempotencyRepository idempotencyRepository,
                                OrderMapper orderMapper,
                                ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.orderMapper = orderMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(String idempotencyKey, CreateOrderRequest request) {
        String requestHash = hashRequest(request);
        OrderIdempotencyEntity existing = idempotencyRepository.findById(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new ConflictException("Idempotency key reused with a different request payload");
            }
            OrderEntity existingOrder = orderRepository.findById(existing.getOrderId())
                    .orElseThrow(() -> new ConflictException("Idempotency record references a missing order"));
            return orderMapper.toResponse(existingOrder);
        }

        List<UUID> productIds = request.items().stream().map(CreateOrderRequest.Item::productId).toList();
        Map<UUID, ProductEntity> productsById;
        try {
            productsById = productRepository.findAllById(productIds)
                    .stream()
                    .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));
        } catch (TransientDataAccessException ex) {
            throw new ServiceUnavailableException("Product catalog is temporarily unavailable. Please retry.");
        }

        for (UUID productId : productIds) {
            if (!productsById.containsKey(productId)) {
                throw new NotFoundException("Product not found: " + productId);
            }
        }

        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.CREATED);
        Instant now = Instant.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setCurrency("USD");

        List<OrderItemEntity> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.Item requestItem : request.items()) {
            ProductEntity product = productsById.get(requestItem.productId());
            if (!product.isAvailable()) {
                throw new ConflictException("Product is not available: " + product.getId());
            }

            OrderItemEntity item = new OrderItemEntity();
            item.setId(UUID.randomUUID());
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setQuantity(requestItem.quantity());
            item.setUnitPriceAmount(product.getPriceAmount());
            item.setLineAmount(product.getPriceAmount().multiply(BigDecimal.valueOf(requestItem.quantity())));
            items.add(item);
            total = total.add(item.getLineAmount());
        }

        order.setTotalAmount(total);
        order.setItems(items);
        OrderEntity savedOrder = orderRepository.save(order);

        OrderIdempotencyEntity idempotency = new OrderIdempotencyEntity();
        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setRequestHash(requestHash);
        idempotency.setOrderId(savedOrder.getId());
        idempotency.setCreatedAt(now);
        idempotencyRepository.save(idempotency);

        return orderMapper.toResponse(savedOrder);
    }

    private String hashRequest(CreateOrderRequest request) {
        try {
            String canonical = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (JacksonException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not hash request", ex);
        }
    }
}


