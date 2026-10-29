package com.example.shopping.orders.infra;

import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    java.util.Optional<OrderEntity> findById(UUID id);
}


