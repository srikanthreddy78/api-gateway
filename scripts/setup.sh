#!/bin/bash

echo "========================================="
echo "🚀 API Gateway Phase 1 Setup"
echo "========================================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker found"
echo "✅ Docker Compose found"
echo ""

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p mock-services
mkdir -p scripts
mkdir -p src/main/resources/lua

echo "✅ Directories created"
echo ""

# Start Docker services
echo "🐳 Starting Docker services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check Redis
echo "🔍 Checking Redis..."
docker exec gateway-redis redis-cli ping

echo ""
echo "========================================="
echo "✅ Setup Complete!"
echo "========================================="
echo ""
echo "Services Running:"
echo "  Redis:            localhost:6379"
echo "  User Service:     localhost:8081"
echo "  Order Service:    localhost:8082"
echo "  Product Service:  localhost:8083"
echo "  Redis Commander:  localhost:8084"
echo ""
echo "Next Steps:"
echo "  1. Build project:  ./mvnw clean package"
echo "  2. Run gateway:    ./mvnw spring-boot:run"
echo "  3. Test:           ./scripts/test-rate-limit.sh"
echo ""
echo "========================================="