package com.ordermanagement.service;

import com.ordermanagement.dto.CreateOrderRequest;
import com.ordermanagement.dto.OrderResponse;
import com.ordermanagement.entity.Order;
import com.ordermanagement.entity.OrderStatus;
import com.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for order management operations.
 * @Task - Handles order creation, retrieval, and coordinates with async processor.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProcessor orderProcessor;

    /**
     * Create a new order with idempotency support.
     * If an order with the same idempotency key exists, return the existing order.
     *
     * @param request        The order creation request
     * @param idempotencyKey Unique key to prevent duplicate orders
     * 
     * @return The created or existing order response
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String idempotencyKey) {
        log.info("Creating order for customer: {} with idempotency key: {}",
                request.getCustomerId(), idempotencyKey);

        // Check for existing order with same idempotency key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOrder.isPresent()) {
                log.info("Order with idempotency key {} already exists. Returning existing order.",
                        idempotencyKey);
                return OrderResponse.fromEntity(existingOrder.get());
            }
        }

        // Create new order
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(OrderStatus.CREATED)
                .idempotencyKey(idempotencyKey)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Trigger async processing
        orderProcessor.processOrder(savedOrder.getId());
        log.info("Async processing triggered for order: {}", savedOrder.getId());

        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Get an order by ID.
     *
     * @param id The order ID
     * @return The order response
     * @throws OrderNotFoundException if order not found
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        log.info("Fetching order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", id);
                    return new OrderNotFoundException("Order not found with ID: " + id);
                });

        return OrderResponse.fromEntity(order);
    }

    /**
     * Get all orders.
     *
     * @return List of all orders
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /**
     * Get orders by customer ID.
     *
     * @param customerId The customer ID
     * @return List of orders for the customer
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /**
     * Exception thrown when an order is not found.
     */
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }
}
