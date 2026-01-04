#!/bin/bash
# GCP Deployment Update Script
# Run this on your GCP server to update to the latest version

set -e

echo "🚀 Updating Order Management Service..."

# Navigate to app directory
cd ~/order-management

# Pull latest code
echo "📥 Pulling latest code from GitHub..."
git pull origin main

# Pull latest Docker image (built by GitHub Actions)
echo "🐳 Pulling latest Docker image..."
docker-compose -f docker-compose.prod.yml pull app

# Restart with new image
echo "♻️  Restarting application..."
docker-compose -f docker-compose.prod.yml up -d

# Wait a bit for startup
echo "⏳ Waiting for application to start..."
sleep 30

# Check health
echo "🏥 Checking application health..."
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Application is healthy!"
    docker-compose -f docker-compose.prod.yml ps
else
    echo "⚠️  Application may still be starting..."
    echo "Check logs with: docker-compose -f docker-compose.prod.yml logs -f"
fi

echo ""
echo "🎉 Update complete!"
echo "Service URL: http://$(curl -s ifconfig.me):8080"
