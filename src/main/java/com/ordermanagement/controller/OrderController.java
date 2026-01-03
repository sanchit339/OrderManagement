package com.ordermanagement.controller;

import com.ordermanagement.dto.CreateOrderRequest;
import com.ordermanagement.dto.OrderResponse;
import com.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order management operations.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders with async processing and idempotency")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order via the Idempotency-Key header.
     *
     * @param request        The order creation request
     * @param idempotencyKey Optional idempotency key to prevent duplicate orders
     * 
     * @return The created order
     */
    @Operation(summary = "Create a new order", description = "Creates an order and processes it asynchronously. Use Idempotency-Key header to prevent duplicates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @Parameter(description = "Unique key to prevent duplicate orders") @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        log.info("Received create order request. Customer: {}, Idempotency-Key: {}",
                request.getCustomerId(), idempotencyKey);

        OrderResponse response = orderService.createOrder(request, idempotencyKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get an order by ID.
     *
     * @param id The order ID
     * @return The order details
     */
    @Operation(summary = "Get order by ID", description = "Retrieves order details including current status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        log.info("Received get order request. ID: {}", id);

        OrderResponse response = orderService.getOrder(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders.
     *
     * @return List of all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Received get all orders request");

        List<OrderResponse> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by customer ID.
     *
     * @param customerId The customer ID
     * @return List of orders for the customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable String customerId) {
        log.info("Received get orders by customer request. Customer: {}", customerId);

        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);

        return ResponseEntity.ok(orders);
    }
}
