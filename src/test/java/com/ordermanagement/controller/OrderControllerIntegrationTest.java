package com.ordermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordermanagement.dto.CreateOrderRequest;
import com.ordermanagement.entity.Order;
import com.ordermanagement.entity.OrderStatus;
import com.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_Success() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("CUST001")
                .productName("Laptop")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "test-key-001")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        // Verify order was saved
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return same order for duplicate idempotency key")
    void createOrder_DuplicateIdempotencyKey_ReturnsSameOrder() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("CUST001")
                .productName("Laptop")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .build();

        String idempotencyKey = "duplicate-key-001";

        // First request
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second request with same key
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Should still have only 1 order
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return validation error for invalid request")
    void createOrder_InvalidRequest_ReturnsValidationError() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("") // Invalid: blank
                .productName("Laptop")
                .quantity(-1) // Invalid: negative
                .price(new BigDecimal("999.99"))
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should get order by ID")
    void getOrder_Success() throws Exception {
        // Create order directly in repository
        Order order = Order.builder()
                .customerId("CUST001")
                .productName("Laptop")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .status(OrderStatus.CREATED)
                .build();
        Order savedOrder = orderRepository.save(order);

        mockMvc.perform(get("/api/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOrder.getId()))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent order")
    void getOrder_NotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should get all orders")
    void getAllOrders_Success() throws Exception {
        // Create two orders
        orderRepository.save(Order.builder()
                .customerId("CUST001")
                .productName("Laptop")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .status(OrderStatus.CREATED)
                .build());

        orderRepository.save(Order.builder()
                .customerId("CUST002")
                .productName("Phone")
                .quantity(2)
                .price(new BigDecimal("599.99"))
                .status(OrderStatus.COMPLETED)
                .build());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
