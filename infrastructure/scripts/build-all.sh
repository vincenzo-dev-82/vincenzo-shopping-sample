#!/bin/bash

# 전체 프로젝트 빌드 스크립트

set -e

echo "🚀 캐시노트 마켓 서비스 빌드 시작..."

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수 정의
print_step() {
    echo -e "${YELLOW}📦 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 루트 디렉토리 확인
if [ ! -f "settings.gradle.kts" ]; then
    print_error "프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# 1. gRPC 공통 모듈 빌드
print_step "gRPC 공통 모듈 빌드 중..."
cd grpc-common
./gradlew clean build publishToMavenLocal
if [ $? -eq 0 ]; then
    print_success "gRPC 공통 모듈 빌드 완료"
else
    print_error "gRPC 공통 모듈 빌드 실패"
    exit 1
fi
cd ..

# 2. 각 서비스 빌드
services=("member-service" "product-service" "order-service" "payment-service")

for service in "${services[@]}"; do
    print_step "$service 빌드 중..."
    cd $service
    ./gradlew clean build
    if [ $? -eq 0 ]; then
        print_success "$service 빌드 완료"
    else
        print_error "$service 빌드 실패"
        exit 1
    fi
    cd ..
done

# 3. Docker 이미지 빌드
print_step "Docker 이미지 빌드 중..."

for service in "${services[@]}"; do
    print_step "$service Docker 이미지 빌드 중..."
    docker build -t vincenzo-$service:latest ./$service/
    if [ $? -eq 0 ]; then
        print_success "$service Docker 이미지 빌드 완료"
    else
        print_error "$service Docker 이미지 빌드 실패"
        exit 1
    fi
done

print_success "🎉 전체 빌드 완료!"
echo ""
echo "다음 명령어로 서비스를 시작할 수 있습니다:"
echo "docker-compose up -d"
echo ""
echo "서비스 확인:"
echo "- Member Service: http://localhost:8081/swagger-ui.html"
echo "- Product Service: http://localhost:8082/swagger-ui.html"
echo "- Order Service: http://localhost:8083/swagger-ui.html"
echo "- Payment Service: http://localhost:8084/swagger-ui.html"
