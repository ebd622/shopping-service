package com.example.shopping.orders.domain;

import com.example.shopping.orders.api.OrderResponse;
import com.example.shopping.orders.infra.OrderEntity;
import com.example.shopping.orders.infra.OrderItemEntity;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getItems().stream()
                        .sorted(Comparator.comparing(OrderItemEntity::getId))
                        .map(i -> new OrderResponse.Item(i.getProductId(), i.getQuantity(), i.getUnitPriceAmount(), i.getLineAmount()))
                        .toList(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCanceledAt()
        );
    }
}

