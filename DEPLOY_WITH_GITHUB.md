# Deployment Using GitHub Container Registry

## 🚀 How It Works

1. **You push code to GitHub**
2. **GitHub Actions automatically:**
   - Runs tests
   - Builds JAR
   - Builds Docker image
   - Pushes to GitHub Container Registry (ghcr.io)
3. **Your GCP server:**
   - Just pulls the pre-built image
   - No build needed on server!

---

## 📦 Make Your Package Public

### Step 1: Push Your Latest Code
```bash
cd /Users/sanchitingale/Development/SpringProject
git add .
git commit -m "setup github container registry"
git push origin main
```

### Step 2: Wait for GitHub Actions
- Go to: https://github.com/sanchit339/OrderManagement/actions
- Watch the workflow run
- Wait for ✅ success

### Step 3: Make Package Public
1. Go to: https://github.com/sanchit339?tab=packages
2. Click on `order-management`
3. Click **"Package settings"** (right sidebar)
4. Scroll to **"Danger Zone"**
5. Click **"Change visibility"**
6. Select **"Public"**
7. Type the package name to confirm

---

## 🖥️ Deploy on GCP Server

### SSH into your server
```bash
# Use regular SSH (since gcloud CLI not installed)
ssh YOUR_USERNAME@YOUR_SERVER_IP
```

Or find connection details in GCP Console.

### Run These Commands on Server

```bash
# 1. Update system
sudo apt update && sudo apt upgrade -y

# 2. Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 3. Install Docker Compose
sudo apt install docker-compose -y

# 4. Logout and login again
exit
# SSH back in

# 5. Configure firewall
sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 8080/tcp
sudo ufw --force enable

# 6. Install fail2ban
sudo apt install fail2ban -y
sudo systemctl enable fail2ban
sudo systemctl start fail2ban

# 7. Clone repository
git clone https://github.com/sanchit339/OrderManagement.git ~/order-management
cd ~/order-management

# 8. Create environment file
cat > .env.prod <<EOF
POSTGRES_DB=orderdb
POSTGRES_USER=orderuser
POSTGRES_PASSWORD=$(openssl rand -base64 32)
EOF
chmod 600 .env.prod

# 9. Pull and start (NO BUILD!)
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# 10. Check status
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f
```

---

## ✅ Advantages of This Approach

| Aspect | Before | After |
|--------|--------|-------|
| Build location | On server (crashes e2-micro) | GitHub Actions (free) |
| Build time | ~5 minutes on e2-micro | ~2 minutes on GitHub |
| Server resources | High usage | Just pulls image |
| Deployment | Manual JAR copy | `docker-compose pull` |
| Professional | Medium | High (CI/CD + registry) |

---

## 🔄 Update Your Application

### On your Mac:
```bash
# Make code changes
git add .
git commit -m "your changes"
git push origin main
```

### GitHub Actions automatically builds

### On GCP server:
```bash
cd ~/order-management
git pull origin main
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

That's it! Server downloads the pre-built image.

---

## 🎯 Interview Talking Points

> "I set up a CI/CD pipeline using GitHub Actions that automatically builds and tests the code on every push. The workflow packages the application into a Docker image and pushes it to GitHub Container Registry. The production server simply pulls the pre-built image, which is much safer for a 1GB instance than building locally. This demonstrates understanding of CI/CD, containerization, and resource-constrained deployments."

**Technical highlights:**
- Automated testing on every push
- Docker image built in CI pipeline
- Published to container registry (GHCR)
- Production server never builds, only pulls
- Zero downtime updates possible

---

## 📊 Your Deployment URLs

- **GitHub Actions**: https://github.com/sanchit339/OrderManagement/actions
- **Container Registry**: https://github.com/sanchit339?tab=packages
- **API**: `http://YOUR_SERVER_IP:8080/api/orders`
- **Swagger UI**: `http://YOUR_SERVER_IP:8080/swagger-ui/index.html`

---

**This is the professional way to deploy!** 🚀
