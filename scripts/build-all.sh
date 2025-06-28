#!/bin/bash

# 전체 서비스 빌드 스크립트

set -e

echo "🚀 캐시노트 마켓 주문 서비스 빌드 시작"

# 루트 디렉토리 확인
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# gRPC 공통 모듈 빌드
echo "📦 gRPC 공통 모듈 빌드 중..."
./gradlew :grpc-common:build

# 각 서비스 빌드
echo "🏗️ 서비스 빌드 중..."
./gradlew :member-service:build
./gradlew :product-service:build
./gradlew :order-service:build
./gradlew :payment-service:build

# Docker 이미지 빌드
echo "🐳 Docker 이미지 빌드 중..."
docker-compose build

echo "✅ 빌드 완료!"
echo "다음 명령어로 서비스를 시작할 수 있습니다:"
echo "  docker-compose up -d"
