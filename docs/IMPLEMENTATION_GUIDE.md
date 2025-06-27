# 구현 설명서

## 프로젝트 개요

캐시노트 마켓 주문 서비스는 **헥사고날 아키텍처(Hexagonal Architecture)**와 **마이크로서비스 패턴**을 적용하여 구현된 다중 결제수단을 지원하는 주문 시스템입니다.

## 아키텍처 설계

### 1. 전체 시스템 구조

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Member        │    │   Product       │    │   Order         │    │   Payment       │
│   Service       │    │   Service       │    │   Service       │    │   Service       │
│   (Port: 8081)  │    │   (Port: 8082)  │    │   (Port: 8083)  │    │   (Port: 8084)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │                       │
         └───────────────────────┼───────────────────────┼───────────────────────┘
                                 │                       │
                          gRPC 통신 (내부)         REST API (외부)
                                 │                       │
         ┌───────────────────────┼───────────────────────┼───────────────────────┐
         │                       │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     MySQL       │    │     Kafka       │    │    Zookeeper    │    │   Load Balancer │
│   (Port: 3306)  │    │   (Port: 9092)  │    │   (Port: 2181)  │    │      (nginx)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 2. 헥사고날 아키텍처 적용

각 서비스는 헥사고날 아키텍처 패턴을 따라 다음과 같이 구성됩니다:

```
┌─────────────────────────────────────────────┐
│                 Adapters                    │
│  ┌─────────────┐            ┌─────────────┐ │
│  │   Inbound   │            │  Outbound   │ │
│  │  (Web/gRPC) │            │ (DB/External│ │
│  └─────────────┘            │   Services) │ │
│         │                   └─────────────┘ │
└─────────┼───────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────┐
│              Application                    │
│  ┌─────────────┐            ┌─────────────┐ │
│  │   Ports     │            │  Services   │ │
│  │  (UseCase)  │◄──────────►│(Business    │ │
│  │             │            │ Logic)      │ │
│  └─────────────┘            └─────────────┘ │
└─────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────┐
│                Domain                       │
│  ┌─────────────┐            ┌─────────────┐ │
│  │   Models    │            │   Events    │ │
│  │             │            │             │ │
│  └─────────────┘            └─────────────┘ │
└─────────────────────────────────────────────┘
```

## 핵심 설계 결정사항

### 1. 결제 시스템 설계 (Strategy Pattern)

복잡한 결제 로직을 처리하기 위해 **Strategy Pattern**을 적용했습니다:

```kotlin
// PaymentProcessor 인터페이스
interface PaymentProcessor {
    fun supports(methodType: PaymentMethodType): Boolean
    suspend fun process(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult
    suspend fun cancel(paymentMethod: PaymentMethod, memberId: Long): PaymentProcessResult
}

// 구현체들
class PGPaymentProcessor : PaymentProcessor { ... }
class CashpointPaymentProcessor : PaymentProcessor { ... }
class CouponPaymentProcessor : PaymentProcessor { ... }
class BNPLPaymentProcessor : PaymentProcessor { ... }
```

**장점**:
- 새로운 결제수단 추가 시 기존 코드 수정 없이 확장 가능
- 각 결제수단별 독립적인 로직 관리
- 테스트 용이성 향상

### 2. 분산 트랜잭션 처리 (Saga Pattern)

마이크로서비스 환경에서 데이터 일관성을 보장하기 위해 **Saga Pattern**을 적용:

```kotlin
// OrderService에서의 주문 생성 프로세스
override fun createOrder(request: CreateOrderRequest): Order {
    return runBlocking {
        // 1. 회원 검증
        val member = memberServiceClient.getMember(request.memberId)
        
        // 2. 상품 및 재고 확인
        val products = productServiceClient.getProducts(productIds)
        
        // 3. 주문 생성
        val order = orderRepository.save(order)
        
        // 4. 결제 처리
        val paymentResult = paymentServiceClient.processPayment(paymentRequest)
        
        if (!paymentResult.success) {
            // 보상 트랜잭션: 주문 취소
            val cancelledOrder = order.cancel()
            orderRepository.save(cancelledOrder)
            publishCancelEvent(cancelledOrder)
            throw IllegalStateException("결제 실패")
        }
        
        // 5. 주문 확정
        val confirmedOrder = order.confirm()
        orderRepository.save(confirmedOrder)
        publishConfirmEvent(confirmedOrder)
        
        confirmedOrder
    }
}
```

### 3. 이벤트 기반 아키텍처

서비스 간 느슨한 결합을 위해 **Event-Driven Architecture** 적용:

```kotlin
// 이벤트 발행
class OrderService {
    fun createOrder(...) {
        // 비즈니스 로직 수행
        val order = ...
        
        // 이벤트 발행
        val event = OrderCreatedEvent(...)
        eventPublisher.publishOrderEvent(event)
    }
}

// 이벤트 소비 (다른 서비스에서)
@KafkaListener(topics = ["order-events"])
fun handleOrderEvent(event: OrderEvent) {
    when (event) {
        is OrderCreatedEvent -> handleOrderCreated(event)
        is OrderCancelledEvent -> handleOrderCancelled(event)
    }
}
```

### 4. 동시성 제어

재고 관리와 캐시포인트 차감에서 동시성 이슈를 해결하기 위해 **낙관적 락**과 **비관적 락**을 적절히 조합:

```kotlin
// 비관적 락을 사용한 캐시포인트 업데이트
@Modifying
@Query("UPDATE MemberEntity m SET m.cashpointBalance = :balance WHERE m.id = :id")
fun updateCashpointBalance(@Param("id") id: Long, @Param("balance") balance: BigDecimal): Int

// 서비스에서 트랜잭션 처리
@Transactional
fun deductCashpoint(...) {
    val success = memberRepository.updateCashpointWithLock(memberId, newBalance)
    if (!success) {
        throw IllegalStateException("동시 업데이트로 인한 실패")
    }
}
```

## 결제 시스템 상세 구현

### 1. 결제 규칙 검증

도메인 모델에서 비즈니스 규칙을 강제합니다:

```kotlin
data class Payment {
    private fun validatePaymentRules() {
        when (paymentType) {
            PaymentType.SINGLE -> {
                require(paymentMethods.size == 1) { "단일 결제는 하나의 결제 방법만 사용할 수 있습니다." }
                val method = paymentMethods.first()
                when (method.methodType) {
                    PaymentMethodType.COUPON -> {
                        throw IllegalArgumentException("쿠폰은 단독 결제가 불가능합니다.")
                    }
                }
            }
            PaymentType.COMBINED -> {
                require(paymentMethods.size > 1) { "복합 결제는 두 개 이상의 결제 방법을 사용해야 합니다." }
                val hasPG = paymentMethods.any { it.methodType == PaymentMethodType.PG }
                require(hasPG) { "복합 결제에는 PG 결제가 포함되어야 합니다." }
                val hasBNPL = paymentMethods.any { it.methodType == PaymentMethodType.BNPL }
                require(!hasBNPL) { "BNPL은 단독 결제만 가능합니다." }
            }
        }
    }
}
```

### 2. Mock 외부 서비스 구현

개발 및 테스트 편의를 위해 외부 서비스들을 Mock으로 구현:

```kotlin
@Component
class PGPaymentProcessor(private val mockConfig: MockPGConfig) : PaymentProcessor {
    override suspend fun process(...): PaymentProcessResult {
        delay(1000) // 네트워크 지연 시뮬레이션
        val isSuccess = Random.nextInt(100) < mockConfig.successRate
        // ...
    }
}

@ConfigurationProperties(prefix = "payment.mock.pg")
class MockPGConfig {
    var enabled: Boolean = true
    var successRate: Int = 90 // 90% 성공률
}
```

## 확장성 고려사항

### 1. 수평 확장 (Horizontal Scaling)

- 각 서비스는 독립적으로 스케일 아웃 가능
- 로드 밸런서를 통한 트래픽 분산
- 상태를 저장하지 않는 Stateless 설계

### 2. 데이터베이스 확장

```sql
-- 샤딩 키 설계 예시
-- 회원 ID 기반 샤딩
CREATE TABLE orders_shard_1 (
    -- member_id % 4 = 1
) PARTITION BY HASH(member_id);

CREATE TABLE orders_shard_2 (
    -- member_id % 4 = 2
) PARTITION BY HASH(member_id);
```

### 3. 캐시 레이어 추가

```kotlin
@Service
class ProductService {
    @Cacheable("products", key = "#productId")
    fun getProduct(productId: Long): Product? {
        return productRepository.findById(productId)
    }
    
    @CacheEvict("products", key = "#product.id")
    fun updateProduct(product: Product): Product {
        return productRepository.save(product)
    }
}
```

## 모니터링 및 관찰가능성

### 1. 헬스체크 엔드포인트

```kotlin
@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now().toString()
        ))
    }
}
```

### 2. 메트릭 수집

```kotlin
@Component
class PaymentMetrics {
    private val paymentCounter = Counter.builder("payment.processed")
        .description("Total processed payments")
        .register(Metrics.globalRegistry)
    
    fun recordPaymentProcessed(methodType: String, success: Boolean) {
        paymentCounter.increment(
            Tags.of(
                "method", methodType,
                "success", success.toString()
            )
        )
    }
}
```

## 보안 고려사항

### 1. 내부 서비스 통신 보안

- gRPC TLS 암호화
- 서비스 간 인증/인가
- API Gateway를 통한 외부 접근 제어

### 2. 민감 데이터 처리

- 결제 정보 암호화
- 개인정보 마스킹
- 감사 로그 기록

## 성능 최적화

### 1. 데이터베이스 최적화

- 적절한 인덱스 설계
- 연관관계 페치 전략 최적화
- 쿼리 성능 모니터링

### 2. 네트워크 최적화

- gRPC HTTP/2 멀티플렉싱 활용
- 연결 풀링
- 압축 활용

## 테스트 전략

### 1. 단위 테스트

```kotlin
@Test
fun `결제 처리 성공 테스트`() {
    // Given
    val payment = createTestPayment()
    val processor = mockk<PaymentProcessor>()
    every { processor.supports(any()) } returns true
    every { processor.process(any(), any()) } returns PaymentProcessResult(true, "성공")
    
    // When
    val result = paymentService.processPayment(request)
    
    // Then
    assertThat(result.success).isTrue()
}
```

### 2. 통합 테스트

```kotlin
@SpringBootTest
@Testcontainers
class OrderIntegrationTest {
    @Container
    val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0")
    
    @Test
    fun `주문 생성 통합 테스트`() {
        // 실제 데이터베이스와 연동된 테스트
    }
}
```

### 3. 계약 테스트 (Contract Testing)

```kotlin
// Pact 또는 Spring Cloud Contract 사용
@Test
fun `회원 서비스 계약 테스트`() {
    // 서비스 간 인터페이스 계약 검증
}
```

이러한 설계를 통해 확장 가능하고 유지보수가 용이한 마이크로서비스 아키텍처를 구현했습니다.
