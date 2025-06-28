#!/bin/bash

# 개발 환경 시작 스크립트

set -e

echo "🚀 Starting development environment..."

# 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

echo "🗄️ Starting infrastructure services (MySQL, Kafka, Zookeeper)..."
docker-compose up -d mysql kafka zookeeper

echo "⏳ Waiting for infrastructure services to be ready..."
sleep 30

echo "🔍 Checking MySQL connection..."
until docker exec vincenzo-mysql mysqladmin ping --silent; do
    echo "Waiting for MySQL..."
    sleep 5
done

echo "🔍 Checking Kafka connection..."
until docker exec vincenzo-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
    echo "Waiting for Kafka..."
    sleep 5
done

echo "📊 Initializing database..."
docker exec -i vincenzo-mysql mysql -uroot -ppassword vincenzo_shopping < infrastructure/init-data.sql || echo "Database already initialized"

echo "🏃 Starting application services..."
docker-compose up -d member-service
sleep 10

docker-compose up -d product-service
sleep 10

docker-compose up -d payment-service
sleep 10

docker-compose up -d order-service
sleep 10

echo "🔍 Checking service health..."
for service in member-service product-service order-service payment-service; do
    echo "Checking $service..."
    until docker-compose ps $service | grep -q "Up"; do
        echo "Waiting for $service..."
        sleep 5
    done
done

echo "✅ Development environment is ready!"
echo ""
echo "📋 Service URLs:"
echo "  - Member Service: http://localhost:8081/swagger-ui.html"
echo "  - Product Service: http://localhost:8082/swagger-ui.html"
echo "  - Order Service: http://localhost:8083/swagger-ui.html"
echo "  - Payment Service: http://localhost:8084/swagger-ui.html"
echo ""
echo "🔍 To check logs: docker-compose logs -f [service-name]"
echo "🛑 To stop all services: docker-compose down"
