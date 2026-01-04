# 🚀 Order Management Service - Setup Guide

Complete step-by-step guide to run this project on your machine.

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Clone Repository](#clone-repository)
3. [Setup Option 1: Local Development](#option-1-local-development-recommended)
4. [Setup Option 2: Full Docker](#option-2-full-docker-everything-in-containers)
5. [Verify Installation](#verify-installation)
6. [API Documentation](#api-documentation)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Minimum Requirements

Choose based on which setup option you'll use:

| Tool | Option 1 (Local Dev) | Option 2 (Full Docker) |
|------|---------------------|------------------------|
| **Docker** | ✅ Required | ✅ Required |
| **Java 17** | ✅ Required | ❌ Not Required |
| **Maven** | ✅ Required | ❌ Not Required |
| **Git** | ✅ Required | ✅ Required |

### Installing Prerequisites

<details>
<summary><b>macOS</b></summary>

```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Docker
brew install --cask docker
# Open Docker.app to start the daemon

# For Option 1 only:
brew install openjdk@17
brew install maven

# Configure Java 17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```
</details>

<details>
<summary><b>Linux (Ubuntu/Debian)</b></summary>

```bash
# Install Docker
sudo apt update
sudo apt install docker.io docker-compose -y
sudo systemctl start docker
sudo usermod -aG docker $USER

# For Option 1 only:
sudo apt install openjdk-17-jdk maven -y

# Verify installations
java -version
mvn -version
docker --version
```
</details>

<details>
<summary><b>Windows</b></summary>

1. **Docker Desktop**: Download from [docker.com](https://www.docker.com/products/docker-desktop)
2. **For Option 1 only**:
   - **Java 17**: Download from [Adoptium](https://adoptium.net/)
   - **Maven**: Download from [maven.apache.org](https://maven.apache.org/download.cgi)
   - Add to PATH environment variables

```powershell
# Verify installations
java -version
mvn -version
docker --version
```
</details>

---

## Clone Repository

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/order-management-service.git

# Navigate to project directory
cd order-management-service

# Verify you're in the right place
ls -la
# You should see: pom.xml, docker-compose.yml, src/, etc.
```

---

## Option 1: Local Development (Recommended)

**Best for:** Development, debugging, testing  
**Requires:** Java 17, Maven, Docker

### Step 1: Start PostgreSQL Database

```bash
# Start PostgreSQL in Docker
docker-compose up -d

# Verify database is running
docker ps
# You should see: order-management-db

# Check database health
docker exec order-management-db pg_isready -U orderuser -d orderdb
# Should output: accepting connections
```

### Step 2: Build the Application

```bash
# Clean and build
mvn clean package -DskipTests

# You should see: BUILD SUCCESS
# JAR created at: target/order-management-service-1.0.0.jar
```

### Step 3: Run the Application

```bash
# Run with Maven (hot-reload enabled)
mvn spring-boot:run

# OR run the JAR directly
java -jar target/order-management-service-1.0.0.jar
```

### Step 4: Verify It's Running

```bash
# In a new terminal, test the health endpoint
curl http://localhost:8080/actuator/health

# Expected output: {"status":"UP",...}
```

**Application URLs:**
- API Base: http://localhost:8080/api/orders
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Stop the Application

```bash
# Stop the app: Ctrl+C in the terminal where it's running

# Stop PostgreSQL
docker-compose down
```

---

## Option 2: Full Docker (Everything in Containers)

**Best for:** Production-like setup, no local Java/Maven needed  
**Requires:** Only Docker

### Step 1: Build and Start All Services

```bash
# Build Docker image and start both app + database
docker-compose -f docker-compose.prod.yml up --build

# Wait for startup messages:
# - "HikariPool-1 - Start completed"
# - "Tomcat started on port 8080"
```

This single command:
- ✅ Builds the Spring Boot application
- ✅ Starts PostgreSQL database
- ✅ Starts the application server
- ✅ Configures networking between containers

### Step 2: Verify It's Running

```bash
# In a new terminal, test the health endpoint
curl http://localhost:8080/actuator/health

# Expected output: {"status":"UP",...}
```

**Application URLs:**
- API Base: http://localhost:8080/api/orders
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Stop the Application

```bash
# Stop all containers: Ctrl+C, then:
docker-compose -f docker-compose.prod.yml down

# Remove volumes (clean database)
docker-compose -f docker-compose.prod.yml down -v
```

---

## Verify Installation

### 1. Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Expected:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### 2. Create an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-12345" \
  -d '{
    "customerId": "CUST001",
    "productName": "Laptop",
    "quantity": 1,
    "price": 999.99
  }'
```

**Expected:**
```json
{
  "id": 1,
  "customerId": "CUST001",
  "productName": "Laptop",
  "status": "CREATED",
  ...
}
```

### 3. Check Order Status (After 3 seconds)

```bash
curl http://localhost:8080/api/orders/1
```

**Expected:** Status should be `COMPLETED` (or `PROCESSING` if still processing)

### 4. Test Idempotency

```bash
# Send the SAME request again with SAME Idempotency-Key
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-12345" \
  -d '{
    "customerId": "CUST001",
    "productName": "Laptop",
    "quantity": 1,
    "price": 999.99
  }'
```

**Expected:** Returns the **same order** (id: 1), no duplicate created!

---

## API Documentation

### Swagger UI (Recommended)

Open in browser: **http://localhost:8080/swagger-ui.html**

- Interactive API testing
- See all endpoints
- Try requests directly from browser
- View request/response schemas

### Postman Collection

Import `postman_collection.json` into Postman for pre-configured API requests.

### Manual Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check |
| POST | `/api/orders` | Create order |
| GET | `/api/orders/{id}` | Get order by ID |
| GET | `/api/orders` | List all orders |
| GET | `/api/orders/customer/{id}` | Get orders by customer |

---

## Troubleshooting

### Issue: Port 8080 already in use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# OR use a different port
export SERVER_PORT=8081
mvn spring-boot:run
```

### Issue: Port 5432 already in use (PostgreSQL)

```bash
# Stop local PostgreSQL if running
# macOS/Linux:
sudo service postgresql stop

# OR modify docker-compose.yml ports to "5433:5432"
```

### Issue: Docker daemon not running

```bash
# macOS: Open Docker Desktop app
open -a Docker

# Linux:
sudo systemctl start docker
```

### Issue: "Cannot find symbol" compilation errors

```bash
# Clean Maven cache and rebuild
mvn clean install -U

# If still failing, check Java version
java -version
# Must be Java 17
```

### Issue: Database connection failed

```bash
# Check if PostgreSQL container is healthy
docker ps

# Check logs
docker logs order-management-db

# Restart database
docker-compose restart
```

### Issue: Tests failing

```bash
# Run tests with detailed output
mvn test -X

# Skip tests if needed (not recommended)
mvn package -DskipTests
```

---

## Next Steps

✅ **Your setup is complete!**

**What to explore:**
1. Open Swagger UI and test APIs interactively
2. Check application logs for async processing
3. Try creating multiple orders and watch status changes
4. Test idempotency with duplicate requests

**For Development:**
- IDE: Import as Maven project in IntelliJ IDEA / Eclipse
- Hot Reload: Use `mvn spring-boot:run` for automatic restarts
- Debugging: Run application in debug mode from your IDE

**For Deployment:**
- Push to GitHub
- Deploy using Docker Compose on cloud (AWS, GCP, Azure)
- Set environment variables for production database

---

## Quick Reference

### Option 1 Commands
```bash
# Start database
docker-compose up -d

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Stop
Ctrl+C (app)
docker-compose down (database)
```

### Option 2 Commands
```bash
# Start everything
docker-compose -f docker-compose.prod.yml up --build

# Stop everything
docker-compose -f docker-compose.prod.yml down
```

---

**Need Help?** Open an issue on GitHub or check the [README.md](README.md) for more details.
