# 기여 가이드

캐시노트 마켓 주문 서비스 프로젝트에 기여해 주셔서 감사합니다! 이 문서는 프로젝트에 기여하는 방법을 안내합니다.

## 시작하기 전에

### 필수 요구사항
- Java 17 이상
- Docker & Docker Compose
- Git

### 권장 요구사항
- IntelliJ IDEA 또는 VS Code
- Kotlin 플러그인
- Docker Desktop

## 개발 환경 설정

### 1. 저장소 포크 및 클론

```bash
# 저장소 포크 후 클론
git clone https://github.com/YOUR_USERNAME/vincenzo-shopping-sample.git
cd vincenzo-shopping-sample

# 업스트림 저장소 추가
git remote add upstream https://github.com/vincenzo-dev-82/vincenzo-shopping-sample.git
```

### 2. 개발 환경 실행

```bash
# 인프라 서비스만 실행 (로컬 개발용)
docker-compose up -d mysql kafka zookeeper

# 또는 전체 환경 실행
docker-compose up -d
```

### 3. 개발 브랜치 생성

```bash
git checkout -b feature/your-feature-name
```

## 코딩 규칙

### Kotlin 코딩 컨벤션

- [Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html) 준수
- 클래스명: PascalCase (예: `OrderService`)
- 함수명: camelCase (예: `createOrder`)
- 상수명: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`)
- 패키지명: 소문자 + 점 표기법 (예: `com.vincenzo.order`)

### 아키텍처 규칙

#### 헥사고날 아키텍처 준수
```
src/main/kotlin/com/vincenzo/{service}/
├── application/          # 비즈니스 로직
│   ├── port/
│   │   ├── in/          # Inbound Port (Use Case)
│   │   └── out/         # Outbound Port (Repository Interface)
│   └── service/         # Service Implementation
├── adapter/             # 어댑터
│   ├── in/
│   │   ├── web/        # REST Controller
│   │   └── grpc/       # gRPC Server
│   └── out/
│       ├── persistence/ # JPA Repository
│       └── external/    # External API Client
└── domain/              # 도메인 모델
    ├── model/
    └── event/
```

#### 의존성 규칙
- Domain → Application → Adapter 방향으로만 의존
- 역방향 의존성 금지
- 인터페이스를 통한 의존성 역전 적용

### 명명 규칙

#### 클래스/인터페이스
- Use Case: `~UseCase` (예: `OrderUseCase`)
- Service: `~Service` (예: `OrderService`)
- Repository: `~Repository` (예: `OrderRepository`)
- Controller: `~Controller` (예: `OrderController`)
- Entity: `~Entity` (예: `OrderEntity`)
- DTO: `~Request`, `~Response` (예: `CreateOrderRequest`)

#### 메서드
- 조회: `get~`, `find~` (예: `getOrder`, `findByOrderNumber`)
- 생성: `create~`, `save~` (예: `createOrder`)
- 수정: `update~`, `modify~` (예: `updateOrderStatus`)
- 삭제: `delete~`, `remove~` (예: `deleteOrder`)
- 검증: `validate~`, `check~` (예: `validatePayment`)

## 커밋 메시지 규칙

### 커밋 메시지 형식
```
<type>(<scope>): <description>

<body>

<footer>
```

### Type 종류
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 변경
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가 또는 수정
- `chore`: 빌드 스크립트, 패키지 매니저 설정 등

### Scope 종류
- `member`: Member Service 관련
- `product`: Product Service 관련
- `order`: Order Service 관련
- `payment`: Payment Service 관련
- `common`: 공통 모듈 관련
- `infra`: 인프라 관련
- `docs`: 문서 관련

### 예시
```
feat(payment): add BNPL payment processor

- Implement BNPLPaymentProcessor with Strategy pattern
- Add mock BNPL service for testing
- Update payment rules validation

Closes #123
```

## 테스트 작성

### 테스트 종류

1. **단위 테스트**: 각 클래스/메서드 테스트
2. **통합 테스트**: 서비스 간 연동 테스트
3. **API 테스트**: REST API 엔드포인트 테스트

### 테스트 작성 규칙

```kotlin
class OrderServiceTest {
    
    @Test
    fun `주문 생성 성공 테스트`() {
        // Given
        val request = createOrderRequest()
        
        // When
        val result = orderService.createOrder(request)
        
        // Then
        assertThat(result.status).isEqualTo(OrderStatus.CONFIRMED)
    }
    
    @Test
    fun `재고 부족 시 주문 생성 실패 테스트`() {
        // Given
        val request = createOrderRequestWithInsufficientStock()
        
        // When & Then
        assertThrows<IllegalStateException> {
            orderService.createOrder(request)
        }
    }
}
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 서비스 테스트
./gradlew :order-service:test

# 특정 테스트 클래스
./gradlew test --tests OrderServiceTest
```

## Pull Request 가이드

### PR 생성 전 체크리스트

- [ ] 코딩 컨벤션 준수
- [ ] 테스트 작성 및 통과
- [ ] 문서 업데이트 (필요시)
- [ ] 커밋 메시지 규칙 준수
- [ ] 빌드 성공 확인

### PR 템플릿

```markdown
## 변경사항 요약
간단한 변경사항 설명

## 변경 타입
- [ ] 새로운 기능
- [ ] 버그 수정
- [ ] 리팩토링
- [ ] 문서 업데이트
- [ ] 기타

## 테스트
- [ ] 단위 테스트 추가/수정
- [ ] 통합 테스트 추가/수정
- [ ] 수동 테스트 완료

## 체크리스트
- [ ] 코딩 컨벤션 준수
- [ ] 자체 코드 리뷰 완료
- [ ] 테스트 통과
- [ ] 문서 업데이트 (필요시)

## 관련 이슈
Closes #이슈번호
```

### 코드 리뷰 가이드

#### 리뷰어를 위한 가이드
- 코드의 가독성과 유지보수성 확인
- 비즈니스 로직의 정확성 검증
- 성능 및 보안 측면 검토
- 테스트 커버리지 확인
- 아키텍처 규칙 준수 여부 확인

#### 작성자를 위한 가이드
- 작은 단위로 PR 생성
- 명확한 설명과 스크린샷 첨부
- 리뷰 피드백에 적극적으로 응답
- 필요시 추가 테스트 작성

## 이슈 관리

### 이슈 템플릿

#### 버그 리포트
```markdown
## 버그 설명
간단한 버그 설명

## 재현 방법
1. 첫 번째 단계
2. 두 번째 단계
3. 버그 발생

## 예상 동작
예상했던 동작 설명

## 실제 동작
실제로 발생한 동작 설명

## 환경
- OS: [예: macOS 13.0]
- Java: [예: 17.0.1]
- Docker: [예: 20.10.17]
```

#### 기능 요청
```markdown
## 기능 설명
원하는 기능에 대한 명확한 설명

## 동기
이 기능이 왜 필요한지 설명

## 상세 설명
기능의 상세한 동작 방식

## 추가 정보
관련 스크린샷, 참고 자료 등
```

## 릴리스 프로세스

### 버전 관리
- Semantic Versioning (Major.Minor.Patch) 사용
- `CHANGELOG.md` 업데이트
- Git 태그를 통한 릴리스 관리

### 릴리스 체크리스트
- [ ] 모든 테스트 통과
- [ ] 문서 업데이트
- [ ] CHANGELOG.md 업데이트
- [ ] 버전 태그 생성
- [ ] Docker 이미지 빌드 및 태그

## 질문 및 도움

- **일반적인 질문**: GitHub Issues를 통해 문의
- **버그 리포트**: 이슈 템플릿을 사용하여 상세히 작성
- **기능 제안**: Feature Request 템플릿 사용

## 라이선스

이 프로젝트에 기여함으로써, 귀하의 기여가 프로젝트와 동일한 MIT 라이선스 하에 있음에 동의하는 것입니다.

---

**감사합니다!** 🎉

여러분의 기여가 이 프로젝트를 더욱 발전시킵니다.
