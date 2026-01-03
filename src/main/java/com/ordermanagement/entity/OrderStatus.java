package com.ordermanagement.entity;

/**
 * Represents the lifecycle status of an order.
 */
public enum OrderStatus {
    CREATED, // Order received, waiting to be processed
    PROCESSING, // Order is being processed
    COMPLETED, // Order processed successfully
    FAILED // Order processing failed
}
