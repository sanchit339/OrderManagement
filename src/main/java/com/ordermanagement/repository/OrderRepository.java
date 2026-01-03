package com.ordermanagement.repository;

import com.ordermanagement.entity.Order;
import com.ordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find an order by its idempotency key.
     * Used to prevent duplicate order creation.
     */
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all orders by customer ID.
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Find all orders by status.
     */
    List<Order> findByStatus(OrderStatus status);
}
