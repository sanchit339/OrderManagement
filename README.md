# Order Management Service

A production-ready backend service for managing order lifecycle, built with Java 17 and Spring Boot 3.

## Features

- **Order CRUD Operations**: Create, retrieve, and list orders
- **Async Processing**: Background order processing with `@Async`
- **Idempotency**: Prevents duplicate orders via `Idempotency-Key` header
- **Transaction Management**: Atomic state updates with `@Transactional`
- **Failure Handling**: Graceful error handling with proper logging
- **Health Checks**: Spring Actuator endpoints for monitoring

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.1 |
| Spring Data JPA | 3.2.x |
| PostgreSQL | 15 |
| Docker | Latest |

## Project Structure

```
src/main/java/com/ordermanagement/
├── OrderManagementApplication.java    # Main entry point
├── config/
│   └── AsyncConfig.java               # Thread pool configuration
├── controller/
│   └── OrderController.java           # REST endpoints
├── dto/
│   ├── CreateOrderRequest.java        # Request validation
│   └── OrderResponse.java             # Response mapping
├── entity/
│   ├── Order.java                     # JPA entity
│   └── OrderStatus.java               # Status enum
├── exception/
│   └── GlobalExceptionHandler.java    # Error handling
├── repository/
│   └── OrderRepository.java           # Data access
└── service/
    ├── OrderProcessor.java            # Async processor
    └── OrderService.java              # Business logic
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for PostgreSQL)

### Setup

1. **Start PostgreSQL**
   ```bash
   docker-compose up -d
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the API**
   - Base URL: `http://localhost:8080`
   - Health: `http://localhost:8080/actuator/health`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create a new order |
| GET | `/api/orders/{id}` | Get order by ID |
| GET | `/api/orders` | List all orders |
| GET | `/api/orders/customer/{id}` | Get orders by customer |

### Create Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-123" \
  -d '{
    "customerId": "CUST001",
    "productName": "Laptop",
    "quantity": 1,
    "price": 999.99
  }'
```

**Response:**
```json
{
  "id": 1,
  "customerId": "CUST001",
  "productName": "Laptop",
  "quantity": 1,
  "price": 999.99,
  "status": "CREATED",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Idempotency

Send the same request with the same `Idempotency-Key` header - you'll get the existing order back (no duplicate created).

## Order Lifecycle

```
CREATED → PROCESSING → COMPLETED
                    ↘ FAILED
```

- **CREATED**: Order received, waiting for processing
- **PROCESSING**: Being processed asynchronously
- **COMPLETED**: Successfully processed
- **FAILED**: Processing failed (reason stored in `failureReason`)

## Running Tests

```bash
# Unit tests
./mvnw test -Dtest=OrderServiceTest

# Integration tests
./mvnw test -Dtest=OrderControllerIntegrationTest

# All tests
./mvnw test
```

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controller    │────│    Service      │────│   Repository    │
│                 │    │                 │    │                 │
│  POST /orders   │    │  createOrder()  │    │  findById()     │
│  GET /orders    │    │  getOrder()     │    │  save()         │
└─────────────────┘    └────────┬────────┘    └─────────────────┘
                                │
                       ┌────────▼────────┐
                       │ Async Processor │
                       │                 │
                       │ processOrder()  │
                       └─────────────────┘
```

## Interview Talking Points

1. **Why idempotency?** - Prevents duplicate orders if client retries
2. **Why @Async?** - API responds fast, processing happens in background
3. **Why @Transactional?** - Ensures atomic state updates, no partial data
4. **Failure handling** - Orders marked FAILED with reason logged
5. **Why PostgreSQL?** - ACID compliance for financial data

## License

MIT
