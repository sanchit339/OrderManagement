package com.ordermanagement.service;

import com.ordermanagement.dto.CreateOrderRequest;
import com.ordermanagement.dto.OrderResponse;
import com.ordermanagement.entity.Order;
import com.ordermanagement.entity.OrderStatus;
import com.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProcessor orderProcessor;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest validRequest;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        validRequest = CreateOrderRequest.builder()
                .customerId("CUST001")
                .productName("Laptop")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .build();

        savedOrder = Order.builder()
                .id(1L)
                .customerId("CUST001")
                .productName("Laptop")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .status(OrderStatus.CREATED)
                .idempotencyKey("test-key-123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create a new order successfully")
    void createOrder_Success() {
        // Given
        String idempotencyKey = "test-key-123";
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderResponse response = orderService.createOrder(validRequest, idempotencyKey);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo("CUST001");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository).save(any(Order.class));
        verify(orderProcessor).processOrder(1L);
    }

    @Test
    @DisplayName("Should return existing order when idempotency key matches")
    void createOrder_IdempotencyKeyExists_ReturnsExisting() {
        // Given
        String idempotencyKey = "existing-key";
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(savedOrder));

        // When
        OrderResponse response = orderService.createOrder(validRequest, idempotencyKey);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);

        // Should not save new order or trigger processing
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderProcessor, never()).processOrder(anyLong());
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void getOrder_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        // When
        OrderResponse response = orderService.getOrder(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void getOrder_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(OrderService.OrderNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should get all orders successfully")
    void getAllOrders_Success() {
        // Given
        Order order2 = Order.builder()
                .id(2L)
                .customerId("CUST002")
                .productName("Phone")
                .quantity(2)
                .price(new BigDecimal("599.99"))
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findAll()).thenReturn(List.of(savedOrder, order2));

        // When
        List<OrderResponse> orders = orderService.getAllOrders();

        // Then
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProductName()).isEqualTo("Laptop");
        assertThat(orders.get(1).getProductName()).isEqualTo("Phone");
    }

    @Test
    @DisplayName("Should get orders by customer ID")
    void getOrdersByCustomer_Success() {
        // Given
        when(orderRepository.findByCustomerId("CUST001")).thenReturn(List.of(savedOrder));

        // When
        List<OrderResponse> orders = orderService.getOrdersByCustomer("CUST001");

        // Then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCustomerId()).isEqualTo("CUST001");
    }
}
