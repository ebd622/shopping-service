package com.example.shopping.orders.infra;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
}

