package com.example.shopping.orders.domain;

import com.example.shopping.orders.api.OrderResponse;
import com.example.shopping.orders.infra.OrderRepository;
import com.example.shopping.shared.error.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderQueryService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }
}


