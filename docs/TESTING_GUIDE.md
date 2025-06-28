# 테스트 가이드

## 테스트 전략

### 1. 테스트 피라미드

```
      ┌─────────────────┐
      │   E2E Tests     │ ← 소수의 핵심 시나리오
      │    (느림)        │
      ├─────────────────┤
      │ Integration     │ ← 주요 API 엔드포인트
      │    Tests        │   서비스 간 통신
      │    (보통)       │
      ├─────────────────┤
      │   Unit Tests    │ ← 비즈니스 로직
      │    (빠름)       │   도메인 모델
      └─────────────────┘   유틸리티 함수
```

### 2. 테스트 범위

- **Unit Tests**: 70%
- **Integration Tests**: 20%
- **E2E Tests**: 10%

## 단위 테스트 (Unit Tests)

### 1. 도메인 모델 테스트

```kotlin
@Test
fun `결제 방법 유효성 검증 - 쿠폰 단독 결제 불가`() {
    // Given
    val paymentMethods = listOf(
        PaymentMethod(
            methodType = PaymentMethodType.COUPON,
            amount = BigDecimal("10000")
        )
    )
    
    // When & Then
    assertThrows<IllegalArgumentException> {
        Payment(
            paymentKey = "PAY123",
            orderId = 1L,
            totalAmount = BigDecimal("10000"),
            paymentType = PaymentType.SINGLE,
            paymentMethods = paymentMethods
        )
    }
}
```

### 2. 서비스 레이어 테스트

```kotlin
@Test
fun `캐시포인트 차감 성공`() {
    // Given
    val memberId = 1L
    val deductAmount = BigDecimal("10000")
    val member = createTestMember(memberId, BigDecimal("50000"))
    
    every { memberRepository.findById(memberId) } returns member
    every { memberRepository.updateCashpointWithLock(memberId, any()) } returns true
    
    // When
    val result = memberService.deductCashpoint(memberId, deductAmount, "TXN123")
    
    // Then
    assertThat(result.cashpointBalance).isEqualTo(BigDecimal("40000"))
    verify { eventPublisher.publishCashpointEvent(any()) }
}
```

### 3. Mock 사용 가이드라인

```kotlin
class PaymentServiceTest {
    private val paymentRepository = mockk<PaymentRepository>()
    private val paymentProcessors = listOf(
        mockk<PGPaymentProcessor>(),
        mockk<CashpointPaymentProcessor>()
    )
    private val eventPublisher = mockk<EventPublisher>(relaxed = true)
    
    private val paymentService = PaymentService(
        paymentRepository,
        paymentProcessors,
        eventPublisher
    )
}
```

## 통합 테스트 (Integration Tests)

### 1. Repository 테스트

```kotlin
@DataJpaTest
@Testcontainers
class MemberRepositoryTest {
    
    @Container
    companion object {
        @JvmStatic
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
    }
    
    @DynamicPropertySource
    companion object {
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
        }
    }
    
    @Autowired
    private lateinit var memberRepository: MemberJpaRepository
    
    @Test
    fun `회원 저장 및 조회`() {
        // Given
        val member = MemberEntity(
            username = "test1",
            email = "test1@example.com",
            name = "테스트 사용자",
            cashpointBalance = BigDecimal("50000")
        )
        
        // When
        val saved = memberRepository.save(member)
        val found = memberRepository.findById(saved.id!!)
        
        // Then
        assertThat(found).isPresent
        assertThat(found.get().username).isEqualTo("test1")
    }
}
```

### 2. Web Layer 테스트

```kotlin
@WebMvcTest(MemberController::class)
class MemberControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var memberUseCase: MemberUseCase
    
    @Test
    fun `회원 조회 API 테스트`() {
        // Given
        val member = createTestMember()
        every { memberUseCase.getMember(1L) } returns member
        
        // When & Then
        mockMvc.perform(get("/api/v1/members/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("test1"))
    }
    
    @Test
    fun `존재하지 않는 회원 조회시 404 반환`() {
        // Given
        every { memberUseCase.getMember(999L) } returns null
        
        // When & Then
        mockMvc.perform(get("/api/v1/members/999"))
            .andExpect(status().isNotFound)
    }
}
```

### 3. gRPC 테스트

```kotlin
@SpringBootTest
@TestPropertySource(properties = [
    "grpc.server.in-process-name=test",
    "grpc.server.port=-1",
    "grpc.client.member-service.address=in-process:test"
])
class MemberGrpcServiceTest {
    
    @Autowired
    private lateinit var memberServiceStub: MemberServiceGrpcKt.MemberServiceCoroutineStub
    
    @MockBean
    private lateinit var memberUseCase: MemberUseCase
    
    @Test
    fun `gRPC 회원 조회 테스트`() = runBlocking {
        // Given
        val member = createTestMember()
        every { memberUseCase.getMember(1L) } returns member
        
        val request = GetMemberRequest.newBuilder()
            .setMemberId(1L)
            .build()
        
        // When
        val response = memberServiceStub.getMember(request)
        
        // Then
        assertThat(response.success).isTrue()
        assertThat(response.member.id).isEqualTo(1L)
    }
}
```

## End-to-End 테스트

### 1. 전체 시나리오 테스트

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderE2ETest {
    
    @Container
    companion object {
        @JvmStatic
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0")
        
        @JvmStatic
        val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
    }
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @Test
    fun `주문 생성부터 완료까지 전체 플로우 테스트`() {
        // Given: 회원과 상품이 존재하는 상태
        val memberId = createTestMember()
        val productId = createTestProduct()
        
        // When: 주문 생성
        val orderRequest = CreateOrderWebRequest(
            memberId = memberId,
            orderItems = listOf(
                OrderItemWebRequest(productId = productId, quantity = 1)
            ),
            paymentInfo = PaymentInfoWebRequest(
                paymentType = "SINGLE",
                paymentMethods = listOf(
                    PaymentMethodWebRequest(
                        methodType = "BNPL",
                        amount = BigDecimal("899000")
                    )
                )
            )
        )
        
        val response = restTemplate.postForEntity(
            "/api/v1/orders",
            orderRequest,
            OrderResponse::class.java
        )
        
        // Then: 주문이 성공적으로 생성되고 확정됨
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.order?.status).isEqualTo(OrderStatus.CONFIRMED)
        
        // And: 재고가 차감됨
        val productResponse = restTemplate.getForEntity(
            "/api/v1/products/$productId",
            ProductResponse::class.java
        )
        assertThat(productResponse.body?.stockQuantity).isEqualTo(49)
    }
}
```

### 2. 결제 실패 시나리오 테스트

```kotlin
@Test
fun `결제 실패시 주문 취소 및 재고 복원 테스트`() {
    // Given: Mock을 통해 결제 실패 시나리오 설정
    mockWebServer.enqueue(
        MockResponse()
            .setResponseCode(400)
            .setBody("{\"success\": false, \"message\": \"결제 실패\"}")
    )
    
    // When: 주문 생성 시도
    val response = restTemplate.postForEntity(
        "/api/v1/orders",
        orderRequest,
        OrderResponse::class.java
    )
    
    // Then: 주문이 실패하고 재고가 복원됨
    assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    
    // 재고 확인
    val productResponse = restTemplate.getForEntity(
        "/api/v1/products/$productId",
        ProductResponse::class.java
    )
    assertThat(productResponse.body?.stockQuantity).isEqualTo(50) // 원래 재고
}
```

## 성능 테스트

### 1. JMeter를 이용한 부하 테스트

```xml
<!-- jmeter-test-plan.jmx -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Order Service Load Test">
      <elementProp name="arguments" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Order Creation">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">100</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">10</stringProp>
        <stringProp name="ThreadGroup.ramp_time">30</stringProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 2. Gatling을 이용한 성능 테스트

```scala
class OrderSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8083")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val orderCreation = scenario("Order Creation")
    .exec(
      http("Create Order")
        .post("/api/v1/orders")
        .body(StringBody(orderJson))
        .check(status.is(201))
    )

  setUp(
    orderCreation.inject(rampUsers(100) during (30 seconds))
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),
      global.successfulRequests.percent.gt(95)
    )
}
```

## 테스트 실행

### 1. 로컬 환경에서 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :member-service:test

# 통합 테스트만 실행
./gradlew integrationTest

# 테스트 리포트 생성
./gradlew test jacocoTestReport
```

### 2. Docker 환경에서 테스트 실행

```bash
# 테스트용 컨테이너 실행
docker-compose -f docker-compose.test.yml up -d

# 테스트 실행
./gradlew test -Dspring.profiles.active=test

# 테스트 환경 정리
docker-compose -f docker-compose.test.yml down -v
```

### 3. CI/CD 파이프라인에서 테스트

```yaml
# .github/workflows/ci.yml에서 설정된 대로
# - 단위 테스트
# - 통합 테스트  
# - 코드 커버리지 리포트
# - 테스트 결과 업로드
```

## 테스트 모범 사례

### 1. AAA 패턴 (Arrange-Act-Assert)

```kotlin
@Test
fun `테스트 설명`() {
    // Arrange (Given)
    val input = createTestInput()
    
    // Act (When)
    val result = service.execute(input)
    
    // Assert (Then)
    assertThat(result).isEqualTo(expected)
}
```

### 2. 테스트 데이터 빌더 패턴

```kotlin
class TestDataBuilder {
    companion object {
        fun member(id: Long = 1L) = MemberBuilder(id)
        fun product(id: Long = 1L) = ProductBuilder(id)
        fun order(id: Long = 1L) = OrderBuilder(id)
    }
}

class MemberBuilder(private val id: Long) {
    private var username = "test$id"
    private var cashpointBalance = BigDecimal("50000")
    
    fun withUsername(username: String) = apply { this.username = username }
    fun withCashpoint(amount: BigDecimal) = apply { this.cashpointBalance = amount }
    
    fun build() = Member(
        id = id,
        username = username,
        email = "$username@example.com",
        name = "테스트 사용자$id",
        cashpointBalance = cashpointBalance,
        status = MemberStatus.ACTIVE
    )
}
```

### 3. 테스트 격리 및 독립성

```kotlin
@TestMethodOrder(OrderAnnotation::class)
class OrderedTest {
    
    @BeforeEach
    fun setUp() {
        // 각 테스트마다 독립적인 환경 설정
        clearAllCaches()
        resetDatabase()
    }
    
    @AfterEach
    fun tearDown() {
        // 테스트 후 정리
        cleanupTestData()
    }
}
```

이 가이드를 통해 신뢰할 수 있고 유지보수가 용이한 테스트 코드를 작성할 수 있습니다.
