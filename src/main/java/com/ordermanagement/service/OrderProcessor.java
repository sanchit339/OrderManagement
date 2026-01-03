package com.ordermanagement.service;

import com.ordermanagement.entity.Order;
import com.ordermanagement.entity.OrderStatus;
import com.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * Asynchronous order processor that handles order processing in the background.
 * This demonstrates async processing patterns commonly used in production
 * systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final Random random = new Random();

    /**
     * Process an order asynchronously.
     * This method runs in a separate thread, allowing the API to respond
     * immediately.
     *
     * @param orderId The ID of the order to process
     */
    @Async("orderProcessorExecutor")
    @Transactional
    public void processOrder(Long orderId) {
        log.info("Starting async processing for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found for processing: {}", orderId);
                    return new RuntimeException("Order not found: " + orderId);
                });

        // Update status to PROCESSING
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        log.info("Order {} status updated to PROCESSING", orderId);

        try {
            // Simulate processing time (e.g., inventory check, payment validation)
            simulateProcessing();

            // Simulate occasional failures (10% failure rate for demo)
            if (shouldSimulateFailure()) {
                throw new RuntimeException("Simulated processing failure - inventory unavailable");
            }

            // Processing successful
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("Order {} processed successfully. Status: COMPLETED", orderId);

        } catch (Exception e) {
            handleProcessingFailure(order, e);
        }
    }

    /**
     * Simulate some processing work.
     */
    private void simulateProcessing() {
        try {
            // Simulate 1-3 seconds of processing
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }

    /**
     * Determine if we should simulate a failure (10% chance).
     */
    private boolean shouldSimulateFailure() {
        return random.nextInt(100) < 10;
    }

    /**
     * Handle processing failure by updating order status and logging.
     */
    private void handleProcessingFailure(Order order, Exception e) {
        log.error("Order {} processing failed: {}", order.getId(), e.getMessage());

        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(e.getMessage());
        orderRepository.save(order);

        log.info("Order {} marked as FAILED. Reason: {}", order.getId(), e.getMessage());
    }
}
