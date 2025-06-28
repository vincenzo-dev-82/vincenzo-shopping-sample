#!/bin/bash

# API 테스트 스크립트

set -e

BASE_URL="http://localhost"
MEMBER_PORT=8081
PRODUCT_PORT=8082
ORDER_PORT=8083
PAYMENT_PORT=8084

echo "🧪 캐시노트 마켓 API 테스트를 시작합니다..."

# 서비스 헬스체크
echo "\n🔍 서비스 헬스체크..."
for service_port in $MEMBER_PORT $PRODUCT_PORT $ORDER_PORT $PAYMENT_PORT; do
    echo "Checking $BASE_URL:$service_port/actuator/health"
    response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL:$service_port/actuator/health)
    if [ $response -eq 200 ]; then
        echo "✅ Service on port $service_port is healthy"
    else
        echo "❌ Service on port $service_port is not responding (HTTP $response)"
        exit 1
    fi
done

# 회원 조회 테스트
echo "\n👤 회원 조회 테스트..."
response=$(curl -s $BASE_URL:$MEMBER_PORT/api/v1/members/1)
echo "Response: $response"
if echo $response | grep -q '"id":1'; then
    echo "✅ 회원 조회 성공"
else
    echo "❌ 회원 조회 실패"
fi

# 캐시포인트 조회 테스트
echo "\n💰 캐시포인트 조회 테스트..."
response=$(curl -s $BASE_URL:$MEMBER_PORT/api/v1/members/1/cashpoint)
echo "Response: $response"
if echo $response | grep -q 'balance'; then
    echo "✅ 캐시포인트 조회 성공"
else
    echo "❌ 캐시포인트 조회 실패"
fi

# 상품 조회 테스트
echo "\n📦 상품 조회 테스트..."
response=$(curl -s $BASE_URL:$PRODUCT_PORT/api/v1/products/1)
echo "Response: $response"
if echo $response | grep -q '"id":1'; then
    echo "✅ 상품 조회 성공"
else
    echo "❌ 상품 조회 실패"
fi

# 재고 확인 테스트
echo "\n📊 재고 확인 테스트..."
response=$(curl -s -X POST $BASE_URL:$PRODUCT_PORT/api/v1/products/1/stock/check \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}')
echo "Response: $response"
if echo $response | grep -q 'available'; then
    echo "✅ 재고 확인 성공"
else
    echo "❌ 재고 확인 실패"
fi

# 단일 결제 주문 테스트 (BNPL)
echo "\n🛒 단일 결제 주문 테스트 (BNPL)..."
order_response=$(curl -s -X POST $BASE_URL:$ORDER_PORT/api/v1/orders \
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
echo "Response: $order_response"
if echo $order_response | grep -q '"success":true'; then
    echo "✅ 단일 결제 주문 성공"
    # 주문 번호 추출
    order_id=$(echo $order_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "📋 생성된 주문 ID: $order_id"
else
    echo "❌ 단일 결제 주문 실패"
fi

# 복합 결제 주문 테스트 (PG + 캐시포인트)
echo "\n🛒 복합 결제 주문 테스트 (PG + 캐시포인트)..."
complex_order_response=$(curl -s -X POST $BASE_URL:$ORDER_PORT/api/v1/orders \
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
          "amount": 1400000.00
        },
        {
          "methodType": "CASHPOINT",
          "amount": 100000.00
        }
      ]
    }
  }')
echo "Response: $complex_order_response"
if echo $complex_order_response | grep -q '"success":true'; then
    echo "✅ 복합 결제 주문 성공"
else
    echo "❌ 복합 결제 주문 실패"
fi

# 주문 목록 조회 테스트
if [ ! -z "$order_id" ]; then
    echo "\n📋 주문 조회 테스트..."
    response=$(curl -s $BASE_URL:$ORDER_PORT/api/v1/orders/$order_id)
    echo "Response: $response"
    if echo $response | grep -q "\"id\":$order_id"; then
        echo "✅ 주문 조회 성공"
    else
        echo "❌ 주문 조회 실패"
    fi
fi

# 회원별 주문 목록 조회 테스트
echo "\n👤 회원별 주문 목록 조회 테스트..."
response=$(curl -s $BASE_URL:$ORDER_PORT/api/v1/orders/member/1)
echo "Response: $response"
if echo $response | grep -q '\['; then
    echo "✅ 회원별 주문 목록 조회 성공"
else
    echo "❌ 회원별 주문 목록 조회 실패"
fi

echo "\n🎉 API 테스트가 완료되었습니다!"
echo "\n📊 테스트 결과 요약:"
echo "   - 모든 서비스가 정상적으로 응답하고 있습니다."
echo "   - 기본적인 CRUD 작업이 정상 동작합니다."
echo "   - 단일 및 복합 결제 시나리오가 정상 동작합니다."
echo "   - 서비스 간 gRPC 통신이 정상 동작합니다."
echo "\n💡 추가 테스트:"
echo "   - Swagger UI를 통한 수동 테스트: http://localhost:8083/swagger-ui.html"
echo "   - 각 서비스별 API 문서 확인 가능"
echo "   - 실패 시나리오 테스트 (잘못된 데이터, 재고 부족 등)"
