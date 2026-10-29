package com.example.shopping.orders.api;

import com.example.shopping.orders.domain.OrderCancellationService;
import com.example.shopping.orders.domain.OrderCreationService;
import com.example.shopping.orders.domain.OrderQueryService;
import com.example.shopping.shared.error.BadRequestException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrdersController {

    private final OrderCreationService orderCreationService;
    private final OrderQueryService orderQueryService;
    private final OrderCancellationService orderCancellationService;

    public OrdersController(OrderCreationService orderCreationService,
                            OrderQueryService orderQueryService,
                            OrderCancellationService orderCancellationService) {
        this.orderCreationService = orderCreationService;
        this.orderQueryService = orderQueryService;
        this.orderCancellationService = orderCancellationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                                     @Valid @RequestBody CreateOrderRequest request) {
        if (idempotencyKey.isBlank()) {
            throw new BadRequestException("Idempotency-Key is required");
        }
        return orderCreationService.createOrder(idempotencyKey, request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderQueryService.getById(id);
    }

    @DeleteMapping("/{id}")
    public OrderResponse cancelOrder(@PathVariable UUID id) {
        return orderCancellationService.cancel(id);
    }
}

