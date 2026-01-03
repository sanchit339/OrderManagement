# Production Deployment Checklist

## Pre-Deployment
- [ ] GCP instance created (e2-micro)
- [ ] SSH access configured
- [ ] GitHub repository is up to date

## Deployment Steps

### 1. SSH into GCP Instance
```bash
gcloud compute ssh YOUR_INSTANCE_NAME --zone=YOUR_ZONE
```

### 2. Run Deployment Script
```bash
# Download and run
curl -fsSL https://raw.githubusercontent.com/sanchit339/OrderManagement/main/deploy-production.sh -o deploy.sh
chmod +x deploy.sh
./deploy.sh
```

### 3. Verify Deployment
```bash
# Check services
docker-compose -f ~/order-management-service/docker-compose.prod.yml ps

# Check health
curl http://localhost:8080/actuator/health

# View logs
docker-compose -f ~/order-management-service/docker-compose.prod.yml logs -f
```

## Post-Deployment Security

### Change Default Passwords
```bash
cd ~/order-management-service
nano .env.prod
# Update POSTGRES_PASSWORD with a strong password
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### Verify Firewall
```bash
sudo ufw status
# Should show: 22/tcp (SSH) and 8080/tcp ALLOW
```

### Check fail2ban
```bash
sudo fail2ban-client status sshd
```

## Monitoring

### View Application Logs
```bash
docker-compose -f ~/order-management-service/docker-compose.prod.yml logs -f app
```

### View Database Logs
```bash
docker-compose -f ~/order-management-service/docker-compose.prod.yml logs -f postgres
```

### Check Resource Usage
```bash
docker stats
free -h
df -h
```

## Maintenance

### Update Application
```bash
cd ~/order-management-service
git pull origin main
docker-compose -f docker-compose.prod.yml up -d --build
```

### Restart Services
```bash
docker-compose -f ~/order-management-service/docker-compose.prod.yml restart
```

### Backup Database
```bash
docker exec order-management-db-prod pg_dump -U orderuser orderdb > backup_$(date +%Y%m%d).sql
```

### Restore Database
```bash
cat backup_YYYYMMDD.sql | docker exec -i order-management-db-prod psql -U orderuser -d orderdb
```

## Production URLs

- **API Base**: `http://YOUR_EXTERNAL_IP:8080/api/orders`
- **Health Check**: `http://YOUR_EXTERNAL_IP:8080/actuator/health`
- **Swagger UI**: `http://YOUR_EXTERNAL_IP:8080/swagger-ui/index.html`

## Troubleshooting

### Services not starting
```bash
docker-compose -f ~/order-management-service/docker-compose.prod.yml logs
docker-compose -f ~/order-management-service/docker-compose.prod.yml down
docker-compose -f ~/order-management-service/docker-compose.prod.yml up -d
```

### Out of memory
```bash
free -h
docker stats
# Consider reducing JVM memory or adding swap
```

### Cannot connect
```bash
# Check firewall
sudo ufw status

# Check if app is running
curl http://localhost:8080/actuator/health

# Check GCP firewall rules
gcloud compute firewall-rules list
```

## Security Best Practices Applied

✅ **Firewall configured** - Only SSH (22) and App (8080) exposed  
✅ **fail2ban installed** - Protects against brute force SSH attacks  
✅ **Database not exposed** - PostgreSQL only accessible via Docker network  
✅ **Secure passwords** - Generated using openssl  
✅ **Environment variables** - Secrets not hardcoded  
✅ **Auto-restart** - Services restart on reboot  
✅ **Resource limits** - Memory limits prevent OOM  

## Interview Talking Points

> "I deployed the service on GCP using Docker Compose with production best practices:
> - Configured UFW firewall to allow only necessary ports
> - Installed fail2ban for SSH protection
> - Used environment variables for secrets
> - Set JVM and PostgreSQL memory limits for the 1GB instance
> - Configured auto-restart on system reboot
> - Database is only accessible via internal Docker network for security"

---

**Deployment Status:**
- [ ] Deployed
- [ ] Health check passing
- [ ] Firewall configured
- [ ] fail2ban running
- [ ] Passwords changed
- [ ] Auto-restart configured
- [ ] Backup procedure tested
