package com.example.shopping.orders.domain;

import com.example.shopping.orders.api.OrderResponse;
import com.example.shopping.orders.infra.OrderEntity;
import com.example.shopping.orders.infra.OrderRepository;
import com.example.shopping.shared.error.ConflictException;
import com.example.shopping.shared.error.NotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCancellationService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderCancellationService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        if (order.getStatus() == OrderStatus.CANCELED) {
            return orderMapper.toResponse(order);
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ConflictException("Order cannot be canceled in current state: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELED);
        Instant now = Instant.now();
        order.setCanceledAt(now);
        order.setUpdatedAt(now);

        return orderMapper.toResponse(orderRepository.save(order));
    }
}


