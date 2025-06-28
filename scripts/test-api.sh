#!/bin/bash

# API 테스트 스크립트

set -e

echo "🧪 API 테스트를 시작합니다..."

BASE_URL="http://localhost"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 테스트 함수
test_endpoint() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    local description=$5
    
    echo -e "${YELLOW}테스트: $description${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" "$url")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "  ${GREEN}✅ 성공: HTTP $http_code${NC}"
        echo "  응답: $(echo $body | jq -r '.' 2>/dev/null || echo $body)"
    else
        echo -e "  ${RED}❌ 실패: 예상 HTTP $expected_status, 실제 HTTP $http_code${NC}"
        echo "  응답: $(echo $body | jq -r '.' 2>/dev/null || echo $body)"
    fi
    echo ""
}

# 서비스 헬스체크
echo "🏥 서비스 헬스체크"
test_endpoint "GET" "$BASE_URL:8081/actuator/health" "" 200 "Member Service 헬스체크"
test_endpoint "GET" "$BASE_URL:8082/actuator/health" "" 200 "Product Service 헬스체크"
test_endpoint "GET" "$BASE_URL:8083/actuator/health" "" 200 "Order Service 헬스체크"
test_endpoint "GET" "$BASE_URL:8084/actuator/health" "" 200 "Payment Service 헬스체크"

# 회원 정보 조회
echo "👤 회원 서비스 테스트"
test_endpoint "GET" "$BASE_URL:8081/api/v1/members/1" "" 200 "회원 정보 조회"
test_endpoint "GET" "$BASE_URL:8081/api/v1/members/1/cashpoint" "" 200 "캐시포인트 조회"

# 상품 정보 조회
echo "📦 상품 서비스 테스트"
test_endpoint "GET" "$BASE_URL:8082/api/v1/products/1" "" 200 "상품 정보 조회"
test_endpoint "GET" "$BASE_URL:8082/api/v1/products?productIds=1,2" "" 200 "상품 목록 조회"

# 재고 확인
stock_check_data='{
  "quantity": 1
}'
test_endpoint "POST" "$BASE_URL:8082/api/v1/products/1/stock/check" "$stock_check_data" 200 "재고 확인"

# 주문 생성 (BNPL 단독 결제)
echo "🛒 주문 서비스 테스트"
order_data='{
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
test_endpoint "POST" "$BASE_URL:8083/api/v1/orders" "$order_data" 201 "BNPL 단독 결제 주문 생성"

# 복합 결제 주문 생성
complex_order_data='{
  "memberId": 2,
  "orderItems": [
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "COMBINED",
    "paymentMethods": [
      {
        "methodType": "PG",
        "amount": 1400000.00
      },
      {
        "methodType": "CASHPOINT",
        "amount": 100000.00
      }
    ]
  }
}'
test_endpoint "POST" "$BASE_URL:8083/api/v1/orders" "$order_data" 201 "복합 결제 주문 생성"

# 잘못된 요청 테스트
echo "❌ 오류 케이스 테스트"
invalid_order_data='{
  "memberId": 999,
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
        "methodType": "COUPON",
        "amount": 899000.00
      }
    ]
  }
}'
test_endpoint "POST" "$BASE_URL:8083/api/v1/orders" "$invalid_order_data" 400 "잘못된 결제방법 (쿠폰 단독 결제)"

echo "🎉 API 테스트가 완료되었습니다!"
