# GCP Deployment Guide - Order Management Service

## ☁️ Deploy on GCP Free Tier (e2-micro)

### Prerequisites
- Google Cloud account with $300 free credits (or Always Free tier)
- `gcloud` CLI installed ([Install guide](https://cloud.google.com/sdk/docs/install))

---

## 🚀 Quick Deployment Steps

### 1. Create GCP VM Instance

```bash
# Login to GCP
gcloud auth login

# Set your project
gcloud config set project YOUR_PROJECT_ID

# Create e2-micro instance (Always Free eligible)
gcloud compute instances create order-service-vm \
  --zone=us-central1-a \
  --machine-type=e2-micro \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --boot-disk-type=pd-standard \
  --tags=http-server
```

### 2. Configure Firewall

```bash
# Allow HTTP traffic
gcloud compute firewall-rules create allow-order-service \
  --allow=tcp:8080 \
  --target-tags=http-server \
  --description="Allow access to Order Management Service"
```

### 3. SSH into VM and Install Docker

```bash
# SSH into your VM
gcloud compute ssh order-service-vm --zone=us-central1-a

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt install docker-compose -y

# Logout and login again for docker permissions
exit
gcloud compute ssh order-service-vm --zone=us-central1-a
```

### 4. Deploy the Application

```bash
# Clone your repository
git clone https://github.com/sanchit339/OrderManagement.git
cd OrderManagement

# Build and start services
docker-compose -f docker-compose.prod.yml up -d --build

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

### 5. Get Your Public URL

```bash
# Get external IP
gcloud compute instances describe order-service-vm \
  --zone=us-central1-a \
  --format='get(networkInterfaces[0].accessConfigs[0].natIP)'
```

Your API will be accessible at: `http://YOUR_EXTERNAL_IP:8080`

---

## 🧪 Test Your Deployment

```bash
# Health check
curl http://YOUR_EXTERNAL_IP:8080/actuator/health

# Swagger UI
# Open in browser: http://YOUR_EXTERNAL_IP:8080/swagger-ui/index.html

# Create an order
curl -X POST http://YOUR_EXTERNAL_IP:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-gcp-001" \
  -d '{
    "customerId": "CUST001",
    "productName": "Laptop",
    "quantity": 1,
    "price": 999.99
  }'
```

---

## 📊 Memory Optimizations Applied

### Spring Boot JVM Tuning
```yaml
JAVA_TOOL_OPTIONS: "-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m"
```
- Heap: 256-512 MB
- Metaspace: 128 MB max
- **Total JVM**: ~512 MB

### PostgreSQL Tuning
```yaml
POSTGRES_SHARED_BUFFERS: 128MB
POSTGRES_MAX_CONNECTIONS: 20
```
- Shared buffers: 128 MB
- Max connections: 20 (reduced from default 100)
- **Total Postgres**: ~256 MB

### Docker Memory Limits
```yaml
app:
  mem_limit: 512m
  mem_reservation: 256m
postgres:
  mem_limit: 256m
  mem_reservation: 128m
```

**Total memory usage: ~768 MB** (leaves 256 MB for OS)

---

## 🛡️ Security Improvements

1. **PostgreSQL not publicly exposed**
   - Removed `ports: 5432:5432`
   - Only accessible via Docker network
   - Better security posture

2. **Internal Docker networking**
   - App connects to `postgres:5432` (internal hostname)
   - No external DB access needed

---

## 💰 Cost Monitoring

### Always Free Tier Limits
- **1 e2-micro** instance per month
- **30 GB** standard persistent disk
- **1 GB** egress to North America (per month)

### Stay Within Free Tier
```bash
# Monitor VM usage
gcloud compute instances describe order-service-vm --zone=us-central1-a

# Check billing
gcloud billing accounts list
```

**⚠️ Warning:** If you exceed free tier limits, you'll be charged. Always monitor usage!

---

## 🔧 Maintenance Commands

```bash
# SSH into VM
gcloud compute ssh order-service-vm --zone=us-central1-a

# View logs
docker-compose -f docker-compose.prod.yml logs -f app

# Restart services
docker-compose -f docker-compose.prod.yml restart

# Update code
git pull
docker-compose -f docker-compose.prod.yml up -d --build

# Stop services
docker-compose -f docker-compose.prod.yml down

# Check resource usage
docker stats
```

---

## 🎯 Interview Talking Points

**When asked about deployment:**

> "I deployed the service on GCP's e2-micro instance (1GB RAM) using Docker Compose. I tuned the JVM to use 512MB max heap and limited PostgreSQL to 256MB. The database isn't publicly exposed - it only communicates with the app via Docker's internal network. This demonstrates understanding of resource constraints and container orchestration."

**Technical highlights:**
- Containerized with Docker for consistency
- Memory-optimized for free-tier deployment
- Internal networking for security
- Health checks for reliability
- Auto-restart on failures

---

## 🗑️ Cleanup (Avoid Charges)

```bash
# Stop services
docker-compose -f docker-compose.prod.yml down -v

# Delete VM instance
gcloud compute instances delete order-service-vm --zone=us-central1-a

# Delete firewall rule
gcloud compute firewall-rules delete allow-order-service
```

---

## ✅ Deployment Checklist

- [ ] GCP project created
- [ ] e2-micro VM running
- [ ] Docker & Docker Compose installed
- [ ] Firewall rule configured (port 8080)
- [ ] Application deployed and running
- [ ] Health check passing
- [ ] Swagger UI accessible
- [ ] Test order creation working
- [ ] Memory usage < 1GB

---

**Your service is now live and resume-ready!** 🚀

Add to resume: "Deployed on GCP with Docker, optimized for 1GB RAM constraints"
