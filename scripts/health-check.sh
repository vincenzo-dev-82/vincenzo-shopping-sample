#!/bin/bash

# 캐시노트 마켓 서비스 헬스체크 스크립트

set -e

echo "🔍 캐시노트 마켓 서비스 헬스체크 시작..."
echo "===================================="

# 서비스 목록
SERVICES=(
    "member-service:8081"
    "product-service:8082"
    "order-service:8083"
    "payment-service:8084"
)

# 인프라 서비스 목록
INFRA_SERVICES=(
    "mysql:3306"
    "kafka:9092"
)

HOST="localhost"
FAILED_SERVICES=()

# 함수: 서비스 상태 확인
check_service() {
    local service_name=$1
    local port=$2
    local endpoint=$3
    
    echo -n "  $service_name ($port) ... "
    
    if curl -f -s "http://$HOST:$port$endpoint" > /dev/null 2>&1; then
        echo "✅ 정상"
        return 0
    else
        echo "❌ 비정상"
        FAILED_SERVICES+=("$service_name")
        return 1
    fi
}

# 함수: 포트 연결 확인
check_port() {
    local service_name=$1
    local port=$2
    
    echo -n "  $service_name ($port) ... "
    
    if nc -z $HOST $port 2>/dev/null; then
        echo "✅ 연결 가능"
        return 0
    else
        echo "❌ 연결 불가"
        FAILED_SERVICES+=("$service_name")
        return 1
    fi
}

# Docker 컨테이너 상태 확인
echo "📦 Docker 컨테이너 상태 확인"
echo "------------------------------"
docker-compose ps
echo ""

# 인프라 서비스 확인
echo "🏗️  인프라 서비스 상태 확인"
echo "---------------------------"
for service_info in "${INFRA_SERVICES[@]}"; do
    IFS=":" read -r service_name port <<< "$service_info"
    check_port "$service_name" "$port"
done
echo ""

# 애플리케이션 서비스 확인
echo "🚀 애플리케이션 서비스 상태 확인"
echo "------------------------------"
for service_info in "${SERVICES[@]}"; do
    IFS=":" read -r service_name port <<< "$service_info"
    check_service "$service_name" "$port" "/actuator/health"
done
echo ""

# 데이터베이스 연결 확인
echo "💾 데이터베이스 연결 확인"
echo "------------------------"
echo -n "  MySQL 데이터베이스 ... "
if docker exec vincenzo-mysql mysql -uroot -ppassword -e "SELECT 1" > /dev/null 2>&1; then
    echo "✅ 연결 정상"
else
    echo "❌ 연결 실패"
    FAILED_SERVICES+=("mysql-connection")
fi
echo ""

# Kafka 연결 확인
echo "📨 Kafka 연결 확인"
echo "-----------------"
echo -n "  Kafka 브로커 ... "
if docker exec vincenzo-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    echo "✅ 연결 정상"
else
    echo "❌ 연결 실패"
    FAILED_SERVICES+=("kafka-connection")
fi
echo ""

# API 엔드포인트 테스트
echo "🔗 API 엔드포인트 테스트"
echo "----------------------"
echo -n "  회원 서비스 API ... "
if curl -f -s "http://$HOST:8081/api/v1/members/1" > /dev/null 2>&1; then
    echo "✅ 정상 응답"
else
    echo "❌ 응답 실패"
    FAILED_SERVICES+=("member-api")
fi

echo -n "  상품 서비스 API ... "
if curl -f -s "http://$HOST:8082/api/v1/products/1" > /dev/null 2>&1; then
    echo "✅ 정상 응답"
else
    echo "❌ 응답 실패"
    FAILED_SERVICES+=("product-api")
fi
echo ""

# 결과 요약
echo "📊 헬스체크 결과 요약"
echo "=================="
if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo "🎉 모든 서비스가 정상 동작 중입니다!"
    echo ""
    echo "📋 접속 정보:"
    echo "  - Member Service Swagger: http://localhost:8081/swagger-ui.html"
    echo "  - Product Service Swagger: http://localhost:8082/swagger-ui.html"
    echo "  - Order Service Swagger: http://localhost:8083/swagger-ui.html"
    echo "  - Payment Service Swagger: http://localhost:8084/swagger-ui.html"
    exit 0
else
    echo "❌ 다음 서비스에 문제가 있습니다:"
    for service in "${FAILED_SERVICES[@]}"; do
        echo "    - $service"
    done
    echo ""
    echo "🔧 트러블슈팅 가이드:"
    echo "  1. 모든 서비스가 시작되었는지 확인: docker-compose ps"
    echo "  2. 로그 확인: docker-compose logs [service-name]"
    echo "  3. 서비스 재시작: docker-compose restart [service-name]"
    echo "  4. 전체 재시작: docker-compose down && docker-compose up -d"
    exit 1
fi
