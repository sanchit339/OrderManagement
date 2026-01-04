# Order Management Service

Backend microservice for managing order lifecycle with asynchronous processing, built with Spring Boot and deployed on GCP.

🌐 **Live Demo**: http://136.113.173.5:8080/swagger-ui/index.html

## Screenshots

![Swagger UI](docs/Live%20Demo%20Screenshot.png)

![GCP Deployment](docs/GCP%20ScreenShot.png)

## Features

- Order creation and retrieval via REST API
- Async background processing using Spring's @Async
- Idempotency support to prevent duplicate orders
- Transaction management for data consistency
- PostgreSQL database with proper indexing
- Interactive API documentation with Swagger UI
- Dockerized deployment
- CI/CD pipeline with GitHub Actions

## Tech Stack

- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Deployment**: GCP e2-micro instance
- **API Docs**: OpenAPI 3.0 (Swagger)

## Quick Start

### Local Development

```bash
# Start database
docker-compose up -d

# Run application
mvn spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

### Production Deployment

See [deployment guide](docs/PRODUCTION_DEPLOYMENT.md) for complete instructions.

## API Testing

### Swagger UI (Interactive)
http://136.113.173.5:8080/swagger-ui/index.html

### Create Order
```bash
curl -X POST http://136.113.173.5:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-123" \
  -d '{
    "customerId": "CUST001",
    "productName": "Laptop",
    "quantity": 1,
    "price": 999.99
  }'
```

### Get Order
```bash
curl http://136.113.173.5:8080/api/orders/1
```

## Key Concepts

### Asynchronous Processing
Orders are processed in background threads, allowing the API to respond immediately while processing happens asynchronously. This improves response times and user experience.

### Idempotency
Using the `Idempotency-Key` header prevents duplicate orders if clients retry requests due to network issues or timeouts. The same key returns the existing order instead of creating a new one.

### Transaction Management
All database operations use `@Transactional` to ensure atomic updates. If processing fails, changes are rolled back to maintain data consistency.

## Architecture

```
┌─────────────┐      HTTP       ┌──────────────┐
│   Client    │ ────────────────▶│ OrderController│
└─────────────┘                  └──────────────┘
                                        │
                                        ▼
                                  ┌──────────────┐
                                  │ OrderService │
                                  └──────────────┘
                                        │
                         ┌──────────────┴──────────────┐
                         ▼                             ▼
                  ┌──────────────┐            ┌──────────────┐
                  │ OrderRepository│          │ OrderProcessor│
                  └──────────────┘            └──────────────┘
                         │                           │
                         ▼                           │
                  ┌──────────────┐                   │
                  │  PostgreSQL  │◀──────────────────┘
                  └──────────────┘
```

## Database Schema

```sql
orders
├── id (bigserial, PK)
├── customer_id (varchar)
├── product_name (varchar)
├── quantity (integer)
├── price (decimal)
├── status (varchar) -- CREATED, PROCESSING, COMPLETED, FAILED
├── idempotency_key (varchar, unique)
├── failure_reason (text)
├── created_at (timestamp)
└── updated_at (timestamp)

Indexes:
- idx_idempotency_key (unique)
- idx_customer_id
- idx_status
```

## Deployment

This project uses a modern CI/CD approach:

1. Code pushed to GitHub
2. GitHub Actions builds JAR and Docker image
3. Image pushed to GitHub Container Registry
4. GCP server pulls pre-built image
5. Docker Compose orchestrates app + database

**Update deployed version:**
```bash
# SSH into GCP server, then:
./update-server.sh
```

See [docs/DEPLOY_WITH_GITHUB.md](docs/DEPLOY_WITH_GITHUB.md) for details.

## Project Structure

```
├── src/main/java/com/ordermanagement/
│   ├── controller/         # REST endpoints
│   ├── service/           # Business logic
│   ├── entity/            # JPA entities
│   ├── repository/        # Data access
│   ├── dto/              # Request/Response objects
│   ├── config/           # Spring configuration
│   └── exception/        # Error handling
├── src/test/             # Unit & integration tests
├── docs/                 # Documentation
├── docker-compose.yml    # Dev environment
├── docker-compose.prod.yml # Production setup
└── Dockerfile           # App container image
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## License

This project is licensed under the MIT License.

---

**Built with Spring Boot** | **Deployed on GCP** | **CI/CD with GitHub Actions**
