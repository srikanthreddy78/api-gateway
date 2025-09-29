# API Gateway - Phase 1

A production-grade API Gateway with rate limiting, request routing, and monitoring.

## 🚀 Features

- ✅ Request routing to multiple backend services
- ✅ Redis-based rate limiting (100 requests/minute)
- ✅ Request/response logging with unique IDs
- ✅ Health checks and metrics
- ✅ Graceful error handling
- ✅ Docker Compose setup

## 📋 Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose

## 🏃 Quick Start

### 1. Clone and Setup
```bash
# Clone repository
git clone <your-repo>
cd api-gateway

# Run setup script
chmod +x scripts/setup.sh
./scripts/setup.sh