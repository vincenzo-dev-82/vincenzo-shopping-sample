#!/bin/bash

# API 테스트 스크립트

set -e

BASE_URL="http://localhost"
MEMBER_PORT="8081"
PRODUCT_PORT="8082"
ORDER_PORT="8083"
PAYMENT_PORT="8084"

echo "🧪 API 테스트 시작"

# 서비스 헬스체크
echo "📋 서비스 헬스체크..."
for port in $MEMBER_PORT $PRODUCT_PORT $ORDER_PORT $PAYMENT_PORT; do
    echo "Checking service on port $port..."
    if curl -f -s "$BASE_URL:$port/actuator/health" > /dev/null; then
        echo "✅ Port $port - OK"
    else
        echo "❌ Port $port - FAILED"
        exit 1
    fi
done

# 회원 조회 테스트
echo "\n👤 회원 조회 테스트..."
response=$(curl -s "$BASE_URL:$MEMBER_PORT/api/v1/members/1")
if echo "$response" | grep -q '"name"'; then
    echo "✅ 회원 조회 성공"
else
    echo "❌ 회원 조회 실패"
    echo "Response: $response"
fi

# 상품 조회 테스트
echo "\n📦 상품 조회 테스트..."
response=$(curl -s "$BASE_URL:$PRODUCT_PORT/api/v1/products/1")
if echo "$response" | grep -q '"name"'; then
    echo "✅ 상품 조회 성공"
else
    echo "❌ 상품 조회 실패"
    echo "Response: $response"
fi

# 단일 결제 주문 테스트
echo "\n🛒 BNPL 단일 결제 주문 테스트..."
order_response=$(curl -s -X POST "$BASE_URL:$ORDER_PORT/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
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
  }')

if echo "$order_response" | grep -q '"success":true'; then
    echo "✅ BNPL 단일 결제 주문 성공"
    order_id=$(echo "$order_response" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
    echo "생성된 주문 ID: $order_id"
else
    echo "❌ BNPL 단일 결제 주문 실패"
    echo "Response: $order_response"
fi

# 복합 결제 주문 테스트
echo "\n💳 복합 결제 주문 테스트 (PG + 캐시포인트)..."
complex_order_response=$(curl -s -X POST "$BASE_URL:$ORDER_PORT/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
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
  }')

if echo "$complex_order_response" | grep -q '"success":true'; then
    echo "✅ 복합 결제 주문 성공"
else
    echo "❌ 복합 결제 주문 실패"
    echo "Response: $complex_order_response"
fi

# 캐시포인트 단독 결제 테스트 (잔액 충분 시)
echo "\n💰 캐시포인트 단독 결제 테스트..."
cashpoint_order_response=$(curl -s -X POST "$BASE_URL:$ORDER_PORT/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 2,
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
          "amount": 80000.00
        }
      ]
    }
  }')

if echo "$cashpoint_order_response" | grep -q '"success":true'; then
    echo "✅ 캐시포인트 단독 결제 성공"
else
    echo "❌ 캐시포인트 단독 결제 실패"
    echo "Response: $cashpoint_order_response"
fi

echo "\n🎉 API 테스트 완료!"
echo "\n📊 테스트 결과 확인을 위한 URL:"
echo "- Member Service Swagger: $BASE_URL:$MEMBER_PORT/swagger-ui.html"
echo "- Product Service Swagger: $BASE_URL:$PRODUCT_PORT/swagger-ui.html"
echo "- Order Service Swagger: $BASE_URL:$ORDER_PORT/swagger-ui.html"
echo "- Payment Service Swagger: $BASE_URL:$PAYMENT_PORT/swagger-ui.html"
echo "- Adminer (DB): $BASE_URL:8080"
echo "- Kafka UI: $BASE_URL:8090"
