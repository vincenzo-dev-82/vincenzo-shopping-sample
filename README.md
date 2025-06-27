# 캐시노트 마켓 주문 서비스

## 프로젝트 개요

멀티모듈 기반의 마이크로서비스 아키텍처로 구현된 캐시노트 마켓 주문 서비스입니다.
각 도메인별로 독립적인 서비스로 분리되어 있으며, gRPC를 통해 내부 통신을 수행합니다.

## 아키텍처

### 서비스 구조
- **member-service**: 회원 관리 서비스 (Port: 8081)
- **product-service**: 상품 관리 서비스 (Port: 8082)
- **order-service**: 주문 관리 서비스 (Port: 8083)
- **payment-service**: 결제 관리 서비스 (Port: 8084)

### 기술 스택
- **Language**: Kotlin
- **Framework**: Spring Boot 3.2.x
- **Database**: MySQL 8.0
- **Message Queue**: Apache Kafka
- **Communication**: gRPC (내부), REST API (외부)
- **Architecture**: Hexagonal Architecture (Port-Adapter Pattern)
- **Containerization**: Docker

### 결제 시스템 특징
- 복합결제 지원 (PG + 포인트/쿠폰)
- BNPL 단독결제
- 캐시노트 포인트 단독결제 (잔액 충분 시)
- 쿠폰 단독결제 불가

## 실행 방법

### 1. 전체 환경 실행
```bash
# Docker Compose로 전체 환경 실행
docker-compose up -d
```

### 2. 개별 서비스 실행
```bash
# 각 서비스 디렉토리에서
./gradlew bootRun
```

### 3. 데이터베이스 초기화
```bash
# 초기 데이터 스크립트 실행
docker exec -i vincenzo-mysql mysql -uroot -ppassword < infrastructure/init-data.sql
```

## API 문서

각 서비스의 Swagger UI는 다음 URL에서 확인할 수 있습니다:
- Member Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html
- Order Service: http://localhost:8083/swagger-ui.html
- Payment Service: http://localhost:8084/swagger-ui.html

## 프로젝트 구조

```
vincenzo-shopping-sample/
├── README.md
├── docker-compose.yml
├── member-service/           # 회원 서비스
│   ├── src/main/kotlin/
│   ├── src/main/resources/
│   ├── build.gradle.kts
│   └── Dockerfile
├── product-service/          # 상품 서비스
│   ├── src/main/kotlin/
│   ├── src/main/resources/
│   ├── build.gradle.kts
│   └── Dockerfile
├── order-service/            # 주문 서비스
│   ├── src/main/kotlin/
│   ├── src/main/resources/
│   ├── build.gradle.kts
│   └── Dockerfile
├── payment-service/          # 결제 서비스
│   ├── src/main/kotlin/
│   ├── src/main/resources/
│   ├── build.gradle.kts
│   └── Dockerfile
├── grpc-common/              # gRPC 공통 모듈
│   ├── src/main/proto/
│   └── build.gradle.kts
└── infrastructure/           # 인프라 설정
    ├── init-data.sql
    └── kafka/
```

## 개발 가이드

### 헥사고날 아키텍처 적용

각 서비스는 다음과 같은 패키지 구조를 따릅니다:

```
src/main/kotlin/com/vincenzo/service/
├── application/              # 비즈니스 로직
│   ├── port/
│   │   ├── in/              # Inbound Port (Use Case)
│   │   └── out/             # Outbound Port (Repository Interface)
│   └── service/             # Service Implementation
├── adapter/                 # 어댑터
│   ├── in/
│   │   ├── web/            # REST Controller
│   │   └── grpc/           # gRPC Server
│   └── out/
│       ├── persistence/     # JPA Repository
│       └── external/        # External API Client
└── domain/                  # 도메인 모델
    ├── model/
    └── event/
```

### gRPC 통신

서비스 간 통신은 gRPC를 사용합니다:
- Proto 파일은 `grpc-common` 모듈에서 관리
- 각 서비스는 gRPC 클라이언트를 통해 다른 서비스 호출
- 비동기 처리가 필요한 경우 Kafka 이벤트 사용

### 데이터 일관성

분산 환경에서의 데이터 일관성을 위해:
- Saga Pattern 적용
- Kafka를 통한 이벤트 기반 아키텍처
- 보상 트랜잭션(Compensation Transaction) 구현

## 확장 가능성

### 트래픽 증가 대응
- 각 서비스별 수평 확장 가능
- 데이터베이스 샤딩
- 캐시 레이어 추가 (Redis)
- 로드 밸런서 적용

### 모니터링
- 각 서비스별 헬스체크 엔드포인트
- 메트릭 수집 (Micrometer)
- 로그 중앙화 (ELK Stack)

## 제약사항

- 현재 구현은 샘플 수준으로, 프로덕션 환경에서는 추가 보안 및 성능 최적화 필요
- 외부 서비스(PG, BNPL 등)는 Mock 구현
- 트랜잭션 관리는 각 서비스별로 독립적으로 처리

## 라이선스

MIT License
