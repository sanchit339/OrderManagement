#!/bin/bash
set -e

echo "🚀 Production Deployment Script for Order Management Service"
echo "============================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }
print_info() { echo -e "${YELLOW}ℹ $1${NC}"; }

# Step 1: System Update
print_info "Step 1: Updating system packages..."
sudo apt update && sudo apt upgrade -y
print_success "System updated"

# Step 2: Install Docker
print_info "Step 2: Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    print_success "Docker installed"
else
    print_info "Docker already installed"
fi

# Step 3: Install Docker Compose
print_info "Step 3: Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    sudo apt install docker-compose -y
    print_success "Docker Compose installed"
else
    print_info "Docker Compose already installed"
fi

# Step 4: Install fail2ban for security
print_info "Step 4: Installing fail2ban for SSH protection..."
sudo apt install fail2ban -y
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
print_success "fail2ban installed and running"

# Step 5: Configure UFW Firewall
print_info "Step 5: Configuring firewall..."
sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 8080/tcp
sudo ufw --force enable
print_success "Firewall configured (SSH + 8080 allowed)"

# Step 6: Create app directory
print_info "Step 6: Creating application directory..."
mkdir -p ~/order-management-service
cd ~/order-management-service
print_success "App directory created"

# Step 7: Clone repository
print_info "Step 7: Cloning repository..."
if [ -d ".git" ]; then
    git pull origin main
    print_info "Repository updated"
else
    git clone https://github.com/sanchit339/OrderManagement.git .
    print_success "Repository cloned"
fi

# Step 8: Create production environment file
print_info "Step 8: Creating production environment file..."
cat > .env.prod <<EOF
# Database Configuration
POSTGRES_DB=orderdb
POSTGRES_USER=orderuser
POSTGRES_PASSWORD=$(openssl rand -base64 32)

# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# JVM Configuration
JAVA_TOOL_OPTIONS=-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m

# PostgreSQL Tuning
POSTGRES_SHARED_BUFFERS=128MB
POSTGRES_MAX_CONNECTIONS=20
EOF
print_success "Environment file created with secure password"

# Step 9: Set proper permissions
print_info "Step 9: Setting file permissions..."
chmod 600 .env.prod
print_success "Secure permissions set on .env.prod"

# Step 10: Pull Docker images
print_info "Step 10: Pulling Docker images..."
docker-compose -f docker-compose.prod.yml pull
print_success "Docker images pulled"

# Step 11: Build application
print_info "Step 11: Building application..."
docker-compose -f docker-compose.prod.yml build --no-cache
print_success "Application built"

# Step 12: Start services
print_info "Step 12: Starting services..."
docker-compose -f docker-compose.prod.yml up -d
print_success "Services started"

# Step 13: Wait for services to be healthy
print_info "Step 13: Waiting for services to be healthy..."
sleep 10

# Check app health
for i in {1..30}; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_success "Application is healthy!"
        break
    fi
    echo "Waiting for app to be ready... ($i/30)"
    sleep 2
done

# Step 14: Display status
print_info "Step 14: Checking deployment status..."
docker-compose -f docker-compose.prod.yml ps

# Step 15: Configure auto-restart on reboot
print_info "Step 15: Configuring auto-restart on reboot..."
cat > ~/start-order-service.sh <<'SCRIPT'
#!/bin/bash
cd ~/order-management-service
docker-compose -f docker-compose.prod.yml up -d
SCRIPT

chmod +x ~/start-order-service.sh

# Add to crontab
(crontab -l 2>/dev/null; echo "@reboot sleep 30 && ~/start-order-service.sh") | crontab -
print_success "Auto-restart configured"

# Final summary
echo ""
echo "============================================================="
print_success "🎉 Deployment Complete!"
echo "============================================================="
echo ""
echo "📊 Service Information:"
echo "  Container Status: $(docker-compose -f ~/order-management-service/docker-compose.prod.yml ps --services | wc -l) containers running"
echo "  App URL: http://$(curl -s ifconfig.me):8080"
echo "  Health Check: http://$(curl -s ifconfig.me):8080/actuator/health"
echo "  Swagger UI: http://$(curl -s ifconfig.me):8080/swagger-ui/index.html"
echo ""
echo "🔐 Security:"
echo "  Firewall: Active (SSH + 8080 only)"
echo "  fail2ban: Protecting SSH"
echo "  Database: Not publicly exposed"
echo "  Password: Stored in .env.prod (secure)"
echo ""
echo "📝 Useful Commands:"
echo "  View logs: docker-compose -f ~/order-management-service/docker-compose.prod.yml logs -f"
echo "  Restart: docker-compose -f ~/order-management-service/docker-compose.prod.yml restart"
echo "  Stop: docker-compose -f ~/order-management-service/docker-compose.prod.yml down"
echo "  Update: cd ~/order-management-service && git pull && docker-compose -f docker-compose.prod.yml up -d --build"
echo ""
print_info "⚠️  IMPORTANT: Logout and login again for docker group to take effect"
echo "============================================================="
