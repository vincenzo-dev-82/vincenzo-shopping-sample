#!/bin/bash

# 전체 프로젝트 테스트 스크립트

set -e

echo "🧪 캐시노트 마켓 서비스 테스트 시작..."

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
print_step() {
    echo -e "${YELLOW}🔍 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# 루트 디렉토리 확인
if [ ! -f "settings.gradle.kts" ]; then
    print_error "프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# 테스트 환경 확인
print_step "테스트 환경 확인 중..."

# Docker 실행 확인
if ! docker info >/dev/null 2>&1; then
    print_error "Docker가 실행되고 있지 않습니다. Docker를 시작해주세요."
    exit 1
fi

# 서비스 실행 확인
print_step "서비스 실행 상태 확인 중..."
docker-compose ps

# 인프라 서비스들이 실행 중인지 확인
services_to_check=("vincenzo-mysql" "vincenzo-kafka" "vincenzo-zookeeper")
for service in "${services_to_check[@]}"; do
    if ! docker-compose ps | grep -q "$service.*Up"; then
        print_info "$service가 실행되지 않았습니다. 인프라 서비스를 시작합니다..."
        docker-compose up -d mysql kafka zookeeper
        sleep 30
        break
    fi
done

# 1. 단위 테스트 실행
print_step "단위 테스트 실행 중..."
services=("member-service" "product-service" "order-service" "payment-service")

for service in "${services[@]}"; do
    print_step "$service 단위 테스트 실행 중..."
    cd $service
    ./gradlew test
    if [ $? -eq 0 ]; then
        print_success "$service 단위 테스트 통과"
    else
        print_error "$service 단위 테스트 실패"
        exit 1
    fi
    cd ..
done

# 2. 통합 테스트를 위한 서비스 시작
print_step "통합 테스트를 위한 서비스 시작 중..."
docker-compose up -d

# 서비스 준비 대기
print_step "서비스 준비 대기 중... (60초)"
sleep 60

# 3. 헬스체크
print_step "서비스 헬스체크 실행 중..."
services_health=("8081" "8082" "8083" "8084")

for port in "${services_health[@]}"; do
    print_step "포트 $port 헬스체크..."
    for i in {1..30}; do
        if curl -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
            print_success "포트 $port 서비스 정상"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "포트 $port 서비스 헬스체크 실패"
            docker-compose logs
            exit 1
        fi
        sleep 2
    done
done

# 4. API 통합 테스트
print_step "API 통합 테스트 실행 중..."

# 회원 조회 테스트
print_step "회원 조회 API 테스트..."
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/members/1)
if [ "$response" -eq 200 ]; then
    print_success "회원 조회 API 테스트 통과"
else
    print_error "회원 조회 API 테스트 실패 (HTTP $response)"
    exit 1
fi

# 상품 조회 테스트
print_step "상품 조회 API 테스트..."
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/products/1)
if [ "$response" -eq 200 ]; then
    print_success "상품 조회 API 테스트 통과"
else
    print_error "상품 조회 API 테스트 실패 (HTTP $response)"
    exit 1
fi

# 주문 생성 테스트 (BNPL)
print_step "주문 생성 API 테스트 (BNPL)..."
order_payload='{
  "memberId": 1,
  "orderItems": [
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "SINGLE",
    "paymentMethods": [
      {
        "methodType": "BNPL",
        "amount": 899000.00
      }
    ]
  }
}'

response=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d "$order_payload" \
  http://localhost:8083/api/v1/orders)

if [ "$response" -eq 201 ]; then
    print_success "주문 생성 API 테스트 통과"
else
    print_error "주문 생성 API 테스트 실패 (HTTP $response)"
    # 디버깅을 위한 응답 출력
    curl -X POST \
      -H "Content-Type: application/json" \
      -d "$order_payload" \
      http://localhost:8083/api/v1/orders
    exit 1
fi

# 복합 결제 테스트
print_step "복합 결제 주문 생성 API 테스트..."
combined_payload='{
  "memberId": 2,
  "orderItems": [
    {
      "productId": 4,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "COMBINED",
    "paymentMethods": [
      {
        "methodType": "PG",
        "amount": 50000.00
      },
      {
        "methodType": "CASHPOINT",
        "amount": 30000.00
      }
    ]
  }
}'

response=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d "$combined_payload" \
  http://localhost:8083/api/v1/orders)

if [ "$response" -eq 201 ]; then
    print_success "복합 결제 주문 생성 API 테스트 통과"
else
    print_error "복합 결제 주문 생성 API 테스트 실패 (HTTP $response)"
    exit 1
fi

# 5. 에러 케이스 테스트
print_step "에러 케이스 테스트..."

# 쿠폰 단독 결제 시도 (실패해야 함)
print_step "쿠폰 단독 결제 에러 케이스 테스트..."
error_payload='{
  "memberId": 1,
  "orderItems": [
    {
      "productId": 4,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "SINGLE",
    "paymentMethods": [
      {
        "methodType": "COUPON",
        "amount": 5000.00
      }
    ]
  }
}'

response=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d "$error_payload" \
  http://localhost:8083/api/v1/orders)

if [ "$response" -eq 400 ]; then
    print_success "쿠폰 단독 결제 에러 케이스 테스트 통과"
else
    print_error "쿠폰 단독 결제 에러 케이스 테스트 실패 (응답: HTTP $response, 예상: 400)"
    exit 1
fi

print_success "🎉 모든 테스트 완료!"
echo ""
print_info "테스트 결과 요약:"
echo "✅ 단위 테스트: 통과"
echo "✅ 헬스체크: 통과"
echo "✅ API 통합 테스트: 통과"
echo "✅ 에러 케이스 테스트: 통과"
echo ""
print_info "서비스는 계속 실행 중입니다. 다음 명령어로 종료할 수 있습니다:"
echo "docker-compose down"
