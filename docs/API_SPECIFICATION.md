# API 명세서

## 개요

캐시노트 마켓 주문 서비스의 REST API 명세서입니다.
각 서비스별로 독립적인 API를 제공하며, Swagger UI를 통해 실시간 문서를 확인할 수 있습니다.

## 서비스별 API 엔드포인트

### Member Service (회원 서비스) - Port 8081
- **Base URL**: `http://localhost:8081/api/v1`
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`

#### 회원 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/members/{memberId}` | 회원 조회 |
| GET | `/members/{memberId}/cashpoint` | 캐시포인트 잔액 조회 |
| POST | `/members/{memberId}/cashpoint/deduct` | 캐시포인트 차감 |
| POST | `/members/{memberId}/cashpoint/refund` | 캐시포인트 환불 |

### Product Service (상품 서비스) - Port 8082
- **Base URL**: `http://localhost:8082/api/v1`
- **Swagger UI**: `http://localhost:8082/swagger-ui.html`

#### 상품 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/{productId}` | 상품 조회 |
| GET | `/products` | 상품 목록 조회 |
| POST | `/products/{productId}/stock/check` | 재고 확인 |

### Order Service (주문 서비스) - Port 8083
- **Base URL**: `http://localhost:8083/api/v1`
- **Swagger UI**: `http://localhost:8083/swagger-ui.html`

#### 주문 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | 주문 생성 |
| GET | `/orders/{orderId}` | 주문 조회 |
| GET | `/orders/order-number/{orderNumber}` | 주문번호로 조회 |
| GET | `/orders/member/{memberId}` | 회원 주문 목록 |
| POST | `/orders/{orderId}/confirm` | 주문 확정 |
| POST | `/orders/{orderId}/cancel` | 주문 취소 |
| POST | `/orders/{orderId}/complete` | 주문 완료 |

### Payment Service (결제 서비스) - Port 8084
- **Base URL**: `http://localhost:8084/api/v1`
- **Swagger UI**: `http://localhost:8084/swagger-ui.html`

#### 결제 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/payments` | 결제 처리 |
| POST | `/payments/{paymentKey}/cancel` | 결제 취소 |
| GET | `/payments/{paymentKey}/status` | 결제 상태 조회 |

## 주요 API 상세 명세

### 1. 주문 생성 API

**Endpoint**: `POST /api/v1/orders`

**Request Body**:
```json
{
  "memberId": 1,
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2
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
        "amount": 99000.00
      }
    ]
  }
}
```

**Response**:
```json
{
  "success": true,
  "message": null,
  "order": {
    "id": 1,
    "orderNumber": "ORD1719462345123",
    "memberId": 1,
    "orderItems": [
      {
        "id": 1,
        "productId": 1,
        "quantity": 2,
        "unitPrice": 899000.00,
        "totalPrice": 1798000.00
      }
    ],
    "totalAmount": 1798000.00,
    "discountAmount": 0.00,
    "finalAmount": 1798000.00,
    "status": "CONFIRMED",
    "createdAt": "2024-06-27T14:30:45",
    "updatedAt": "2024-06-27T14:30:46"
  }
}
```

### 2. 결제 처리 API

**Endpoint**: `POST /api/v1/payments`

**Request Body**:
```json
{
  "paymentKey": "PAY_ORD1719462345123_1719462346789",
  "orderId": 1,
  "memberId": 1,
  "totalAmount": 899000.00,
  "paymentType": "SINGLE",
  "paymentMethods": [
    {
      "methodType": "BNPL",
      "amount": 899000.00,
      "additionalInfo": {}
    }
  ]
}
```

**Response**:
```json
{
  "success": true,
  "message": "결제가 성공적으로 완료되었습니다.",
  "payment": {
    "id": 1,
    "paymentKey": "PAY_ORD1719462345123_1719462346789",
    "orderId": 1,
    "totalAmount": 899000.00,
    "paymentType": "SINGLE",
    "status": "COMPLETED",
    "paymentMethods": [
      {
        "id": 1,
        "methodType": "BNPL",
        "amount": 899000.00,
        "status": "COMPLETED",
        "externalTransactionId": "BNPL_1_1719462347890"
      }
    ],
    "createdAt": "2024-06-27T14:32:26",
    "updatedAt": "2024-06-27T14:32:28"
  }
}
```

## 결제 방법별 제약사항

### 단일 결제 (SINGLE)
- **PG**: 단독 결제 가능
- **CASHPOINT**: 단독 결제 가능 (잔액 충분 시)
- **BNPL**: 단독 결제 가능
- **COUPON**: 단독 결제 불가

### 복합 결제 (COMBINED)
- 반드시 PG가 메인 결제수단으로 포함되어야 함
- CASHPOINT, COUPON을 하위 결제수단으로 사용 가능
- BNPL은 복합 결제 불가

## 에러 코드

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | BAD_REQUEST | 잘못된 요청 파라미터 |
| 404 | NOT_FOUND | 리소스를 찾을 수 없음 |
| 409 | CONFLICT | 비즈니스 로직 위반 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

## 테스트 데이터

### 회원 정보
- **test1** (ID: 1): 캐시포인트 50,000원
- **test2** (ID: 2): 캐시포인트 100,000원

### 상품 정보
- **스마트폰** (ID: 1): 899,000원, 재고 50개
- **노트북** (ID: 2): 1,500,000원, 재고 30개
- **이어폰** (ID: 3): 150,000원, 재고 100개
- **마우스** (ID: 4): 80,000원, 재고 200개

### 쿠폰 정보
- **WELCOME10**: 10% 할인 (최대 50,000원)
- **FIXED5000**: 5,000원 고정할인
- **VIP20**: 20% 할인 (최대 100,000원)
