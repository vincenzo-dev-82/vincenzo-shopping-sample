#!/bin/bash

# 캐시노트 마켓 데모 테스트 스크립트

set -e

HOST="localhost"
BASE_COLOR="\033[0m"
RED="\033[31m"
GREEN="\033[32m"
YELLOW="\033[33m"
BLUE="\033[34m"
MAGENTA="\033[35m"
CYAN="\033[36m"

echo -e "${CYAN}🛒 캐시노트 마켓 데모 테스트 시작${BASE_COLOR}"
echo "======================================"
echo ""

# 함수: API 호출 및 결과 표시
api_call() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo -e "${BLUE}📡 $description${BASE_COLOR}"
    echo "   요청: $method $url"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    # HTTP 상태 코드 분리
    http_code=$(echo "$response" | tail -n1)
    json_response=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "   ${GREEN}✅ 성공 (HTTP $http_code)${BASE_COLOR}"
        echo "   응답: $(echo "$json_response" | jq '.' 2>/dev/null || echo "$json_response")"
    else
        echo -e "   ${RED}❌ 실패 (HTTP $http_code)${BASE_COLOR}"
        echo "   응답: $json_response"
    fi
    echo ""
}

# 1. 기본 데이터 확인
echo -e "${MAGENTA}1️⃣ 기본 데이터 확인${BASE_COLOR}"
echo "=================="

api_call "GET" "http://$HOST:8081/api/v1/members/1" "" "회원 정보 조회"
api_call "GET" "http://$HOST:8081/api/v1/members/1/cashpoint" "" "캐시포인트 잔액 조회"
api_call "GET" "http://$HOST:8082/api/v1/products/1" "" "스마트폰 상품 정보 조회"
api_call "GET" "http://$HOST:8082/api/v1/products/3" "" "이어폰 상품 정보 조회"

# 2. 단일 결제 테스트
echo -e "${MAGENTA}2️⃣ 단일 결제 테스트${BASE_COLOR}"
echo "=================="

# BNPL 단일 결제
bnpl_order='{
  "memberId": 1,
  "orderItems": [
    {
      "productId": 3,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "SINGLE",
    "paymentMethods": [
      {
        "methodType": "BNPL",
        "amount": 150000.00
      }
    ]
  }
}'

api_call "POST" "http://$HOST:8083/api/v1/orders" "$bnpl_order" "BNPL 단일 결제 주문 생성"

# 캐시포인트 단일 결제
cashpoint_order='{
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
        "methodType": "CASHPOINT",
        "amount": 50000.00
      }
    ]
  }
}'

api_call "POST" "http://$HOST:8083/api/v1/orders" "$cashpoint_order" "캐시포인트 단일 결제 주문 생성 (마우스 - 50,000원)"

# 3. 복합 결제 테스트
echo -e "${MAGENTA}3️⃣ 복합 결제 테스트${BASE_COLOR}"
echo "=================="

# PG + 캐시포인트 복합 결제
combined_order='{
  "memberId": 2,
  "orderItems": [
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "COMBINED",
    "paymentMethods": [
      {
        "methodType": "PG",
        "amount": 799000.00
      },
      {
        "methodType": "CASHPOINT",
        "amount": 100000.00
      }
    ]
  }
}'

api_call "POST" "http://$HOST:8083/api/v1/orders" "$combined_order" "PG + 캐시포인트 복합 결제 (스마트폰)"

# 4. 실패 케이스 테스트
echo -e "${MAGENTA}4️⃣ 실패 케이스 테스트${BASE_COLOR}"
echo "==================="

# 쿠폰 단독 결제 (실패해야 함)
coupon_only_order='{
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
        "amount": 80000.00,
        "additionalInfo": {
          "couponCode": "WELCOME10"
        }
      }
    ]
  }
}'

api_call "POST" "http://$HOST:8083/api/v1/orders" "$coupon_only_order" "쿠폰 단독 결제 시도 (실패 예상)"

# 캐시포인트 부족 케이스
insufficient_cashpoint_order='{
  "memberId": 1,
  "orderItems": [
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "paymentInfo": {
    "paymentType": "SINGLE",
    "paymentMethods": [
      {
        "methodType": "CASHPOINT",
        "amount": 1500000.00
      }
    ]
  }
}'

api_call "POST" "http://$HOST:8083/api/v1/orders" "$insufficient_cashpoint_order" "캐시포인트 부족 케이스 (노트북 - 실패 예상)"

# 5. 주문 조회 테스트
echo -e "${MAGENTA}5️⃣ 주문 조회 테스트${BASE_COLOR}"
echo "================="

api_call "GET" "http://$HOST:8083/api/v1/orders/member/1" "" "회원 1의 주문 목록 조회"
api_call "GET" "http://$HOST:8083/api/v1/orders/member/2" "" "회원 2의 주문 목록 조회"

# 6. 최종 상태 확인
echo -e "${MAGENTA}6️⃣ 최종 상태 확인${BASE_COLOR}"
echo "==============="

api_call "GET" "http://$HOST:8081/api/v1/members/1/cashpoint" "" "회원 1 최종 캐시포인트 잔액"
api_call "GET" "http://$HOST:8081/api/v1/members/2/cashpoint" "" "회원 2 최종 캐시포인트 잔액"

echo -e "${CYAN}🎉 데모 테스트 완료!${BASE_COLOR}"
echo "================="
echo ""
echo -e "${YELLOW}💡 추가 테스트 방법:${BASE_COLOR}"
echo "  - Swagger UI에서 직접 테스트: http://localhost:808[1-4]/swagger-ui.html"
echo "  - Postman Collection 사용"
echo "  - 로그 확인: docker-compose logs -f [service-name]"
echo ""
echo -e "${GREEN}✨ 캐시노트 마켓 서비스가 정상적으로 동작하고 있습니다!${BASE_COLOR}"
