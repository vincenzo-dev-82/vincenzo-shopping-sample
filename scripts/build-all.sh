#!/bin/bash

# 전체 프로젝트 빌드 스크립트

set -e

echo "🚀 Building Vincenzo Shopping Sample Project..."

# 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

echo "📦 Building gRPC common module..."
cd grpc-common
./gradlew build publishToMavenLocal
cd ..

echo "👤 Building member service..."
cd member-service
./gradlew build
cd ..

echo "📦 Building product service..."
cd product-service
./gradlew build
cd ..

echo "📋 Building order service..."
cd order-service
./gradlew build
cd ..

echo "💳 Building payment service..."
cd payment-service
./gradlew build
cd ..

echo "🐳 Building Docker images..."
docker-compose build

echo "✅ Build completed successfully!"
echo "🏃 To run the services: docker-compose up -d"
