#!/bin/bash

# 캐시노트 마켓 주문 서비스 API 테스트 스크립트

set -e

echo "🚀 캐시노트 마켓 API 테스트 시작"
echo "======================================"

# 서비스 헬스체크
echo "\n📊 서비스 상태 확인"
echo "Member Service Health:"
curl -s http://localhost:8081/actuator/health | jq .

echo "\nProduct Service Health:"
curl -s http://localhost:8082/actuator/health | jq .

echo "\nOrder Service Health:"
curl -s http://localhost:8083/actuator/health | jq .

echo "\nPayment Service Health:"
curl -s http://localhost:8084/actuator/health | jq .

# 기본 데이터 조회
echo "\n👤 회원 정보 조회 (ID: 1)"
curl -s http://localhost:8081/api/v1/members/1 | jq .

echo "\n📦 상품 정보 조회 (ID: 1)"
curl -s http://localhost:8082/api/v1/products/1 | jq .

echo "\n💰 회원 캐시포인트 조회 (ID: 1)"
curl -s http://localhost:8081/api/v1/members/1/cashpoint | jq .

# 단일 결제 주문 테스트
echo "\n🛒 단일 BNPL 결제 주문 생성"
ORDER_RESPONSE=$(curl -s -X POST "http://localhost:8083/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
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
  }')

echo $ORDER_RESPONSE | jq .

# 주문 ID 추출
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.order.id')
echo "\n📋 생성된 주문 ID: $ORDER_ID"

# 복합 결제 주문 테스트
echo "\n🛒 복합 결제 주문 생성 (PG + 캐시포인트)"
COMBINED_ORDER_RESPONSE=$(curl -s -X POST "http://localhost:8083/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
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
          "amount": 1450000.00
        },
        {
          "methodType": "CASHPOINT",
          "amount": 50000.00
        }
      ]
    }
  }')

echo $COMBINED_ORDER_RESPONSE | jq .

# 회원별 주문 목록 조회
echo "\n📝 회원 주문 목록 조회 (회원 ID: 1)"
curl -s http://localhost:8083/api/v1/orders/member/1 | jq .

echo "\n✅ API 테스트 완료!"
echo "======================================"
echo "🔗 Swagger UI 링크:"
echo "   - Member Service: http://localhost:8081/swagger-ui.html"
echo "   - Product Service: http://localhost:8082/swagger-ui.html"
echo "   - Order Service: http://localhost:8083/swagger-ui.html"
echo "   - Payment Service: http://localhost:8084/swagger-ui.html"
