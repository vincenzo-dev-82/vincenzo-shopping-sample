#!/bin/bash

# 캐시노트 마켓 주문 서비스 시작 스크립트

set -e

echo "🚀 캐시노트 마켓 주문 서비스를 시작합니다..."

# 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

# 기존 컨테이너 정리
echo "🧹 기존 컨테이너를 정리합니다..."
docker-compose down -v

# 서비스 시작
echo "🌟 서비스를 시작합니다..."
docker-compose up -d

# 서비스 준비 대기
echo "⏳ 서비스가 준비될 때까지 기다립니다..."
sleep 30

# 헬스체크
echo "🏥 서비스 상태를 확인합니다..."
services=("member-service:8081" "product-service:8082" "order-service:8083" "payment-service:8084")

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    echo "  - $name 확인 중..."
    
    max_attempts=30
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "    ✅ $name 준비 완료"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            echo "    ❌ $name 시작 실패"
            echo "    로그를 확인하세요: docker-compose logs $name"
        else
            echo "    ⏳ $name 시작 중... ($attempt/$max_attempts)"
            sleep 2
        fi
        
        ((attempt++))
    done
done

echo ""
echo "🎉 캐시노트 마켓 주문 서비스가 시작되었습니다!"
echo ""
echo "📋 서비스 URL:"
echo "  - Member Service Swagger: http://localhost:8081/swagger-ui.html"
echo "  - Product Service Swagger: http://localhost:8082/swagger-ui.html"
echo "  - Order Service Swagger: http://localhost:8083/swagger-ui.html"
echo "  - Payment Service Swagger: http://localhost:8084/swagger-ui.html"
echo ""
echo "🔍 서비스 상태 확인: docker-compose ps"
echo "📄 로그 확인: docker-compose logs -f [service-name]"
echo "🛑 서비스 중지: docker-compose down"
