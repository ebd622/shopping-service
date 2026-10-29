package com.example.shopping.orders.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderIdempotencyRepository extends JpaRepository<OrderIdempotencyEntity, String> {
}

