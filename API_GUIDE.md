# API 가이드

## 서비스 포트

- **Member Service**: http://localhost:8081 (gRPC: 9091)
- **Product Service**: http://localhost:8082 (gRPC: 9092)
- **Order Service**: http://localhost:8083 (gRPC: 9093)
- **Payment Service**: http://localhost:8084 (gRPC: 9094)

## API 예제

### 1. 회원 조회

```bash
curl -X GET "http://localhost:8081/api/v1/members/1"
```

### 2. 상품 조회

```bash
curl -X GET "http://localhost:8082/api/v1/products/1"
```

### 3. 주문 생성 (복합결제)

```bash
curl -X POST "http://localhost:8083/api/v1/orders" \
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
      "paymentType": "COMBINED",
      "paymentMethods": [
        {
          "methodType": "PG",
          "amount": 800000.00
        },
        {
          "methodType": "CASHPOINT",
          "amount": 50000.00
        },
        {
          "methodType": "COUPON",
          "amount": 49000.00,
          "additionalInfo": {
            "couponCode": "WELCOME10"
          }
        }
      ]
    }
  }'
```

### 4. 단일 캐시포인트 결제

```bash
curl -X POST "http://localhost:8083/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 2,
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
          "methodType": "CASHPOINT",
          "amount": 150000.00
        }
      ]
    }
  }'
```

### 5. BNPL 단독 결제

```bash
curl -X POST "http://localhost:8083/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
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
          "methodType": "BNPL",
          "amount": 1500000.00
        }
      ]
    }
  }'
```

### 6. 주문 조회

```bash
curl -X GET "http://localhost:8083/api/v1/orders/1"
```

### 7. 회원의 주문 목록 조회

```bash
curl -X GET "http://localhost:8083/api/v1/orders/member/1"
```

### 8. 주문 취소

```bash
curl -X POST "http://localhost:8083/api/v1/orders/1/cancel" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "고객 변심"
  }'
```

### 9. 캐시포인트 잔액 조회

```bash
curl -X GET "http://localhost:8081/api/v1/members/1/cashpoint"
```

### 10. 캐시포인트 차감 (직접)

```bash
curl -X POST "http://localhost:8081/api/v1/members/1/cashpoint/deduct" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000.00,
    "transactionId": "TEST_DEDUCT_001"
  }'
```

## 결제 규칙 검증

### 성공 케이스
- 단일 PG 결제
- 단일 BNPL 결제
- 단일 캐시포인트 결제 (잔액 충분 시)
- 복합결제 (PG + 캐시포인트)
- 복합결제 (PG + 쿠폰)
- 복합결제 (PG + 캐시포인트 + 쿠폰)

### 실패 케이스
- 쿠폰 단독 결제 시도
- 복합결제에 BNPL 포함
- 복합결제에 PG 미포함
- 캐시포인트 잔액 부족
- 존재하지 않는 상품 주문
- 재고 부족 상품 주문

## Swagger UI

각 서비스의 상세한 API 문서는 Swagger UI에서 확인할 수 있습니다:

- Member Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html  
- Order Service: http://localhost:8083/swagger-ui.html
- Payment Service: http://localhost:8084/swagger-ui.html
