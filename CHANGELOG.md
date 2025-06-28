# Changelog

## [1.0.0] - 2024-06-27

### Added
- 초기 프로젝트 구조 생성
- 헥사고날 아키텍처 기반 멀티모듈 마이크로서비스 구현
- 회원 서비스 (member-service)
  - 회원 정보 관리
  - 캐시포인트 관리 (차감/환불)
  - gRPC 서버 구현
- 상품 서비스 (product-service)
  - 상품 정보 관리
  - 재고 관리 (확인/차감/복원)
  - gRPC 서버 구현
- 주문 서비스 (order-service)
  - 주문 생성/조회/관리
  - 복합 결제 로직 구현
  - Saga 패턴 기반 분산 트랜잭션 처리
  - gRPC 클라이언트를 통한 다른 서비스 연동
- 결제 서비스 (payment-service)
  - Strategy 패턴 기반 결제 처리기 구현
  - PG, 캐시포인트, 쿠폰, BNPL 결제 지원
  - Mock 외부 서비스 구현
  - 보상 트랜잭션 처리
- 공통 모듈 (grpc-common)
  - gRPC 프로토콜 정의
  - 서비스 간 통신 인터페이스
- Docker 기반 개발 환경
  - MySQL 8.0
  - Apache Kafka + Zookeeper
  - Docker Compose 설정
- 포괄적인 문서화
  - API 명세서
  - 데이터베이스 ERD
  - 구현 가이드
  - 배포 가이드

### Features
- **결제 시스템 특징**:
  - 단일 결제: PG, 캐시포인트, BNPL 지원
  - 복합 결제: PG + 캐시포인트/쿠폰 조합 지원
  - 쿠폰 단독 결제 불가
  - BNPL 단독 결제만 가능
- **아키텍처 특징**:
  - 헥사고날 아키텍처 (Port-Adapter Pattern)
  - SOLID 원칙 준수
  - Strategy 패턴을 통한 결제 처리기 확장
  - Event-Driven Architecture
  - Saga 패턴을 통한 분산 트랜잭션 관리
- **기술 스택**:
  - Kotlin + Spring Boot 3.2
  - gRPC 통신
  - MySQL 8.0
  - Apache Kafka
  - Docker & Docker Compose
  - Swagger/OpenAPI 3.0

### Technical Debt
- 외부 서비스는 Mock 구현 (실제 PG, BNPL 연동 필요)
- 보안 강화 필요 (인증/인가, 암호화)
- 로그 중앙화 시스템 미구현
- 모니터링 시스템 미구현
- 부하 테스트 미실시

### Known Issues
- gradlew 스크립트 권한 설정 필요 (chmod +x gradlew)
- 프로덕션 환경 설정 미완성
- 성능 최적화 미완료

---

## 향후 계획

### [1.1.0] - 계획 중
- 실제 PG 연동
- Redis 캐시 레이어 추가
- 모니터링 시스템 구축 (Prometheus + Grafana)
- 로그 중앙화 (ELK Stack)
- API Gateway 구현
- Circuit Breaker 패턴 적용
- 부하 테스트 및 성능 최적화

### [1.2.0] - 계획 중
- 프론트엔드 구현 (React/Vue.js)
- 실시간 알림 시스템
- 쿠폰 시스템 고도화
- 재고 예약 시스템
- 배송 관리 시스템
- 환불 시스템 고도화

### [2.0.0] - 계획 중
- Kubernetes 배포
- 멀티 리전 지원
- CQRS + Event Sourcing 적용
- GraphQL API 추가
- 머신러닝 기반 추천 시스템
