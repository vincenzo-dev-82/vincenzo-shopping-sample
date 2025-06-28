# 변경 이력

모든 주목할 만한 변경사항들이 이 파일에 문서화됩니다.

## [1.0.0] - 2024-06-27

### 추가됨
- 🎉 초기 프로젝트 구조 생성
- 🏗️ 마이크로서비스 아키텍처 구현
  - Member Service (회원 관리)
  - Product Service (상품 관리)
  - Order Service (주문 관리)
  - Payment Service (결제 관리)
- 🔧 헥사고날 아키텍처 적용
- 🚀 gRPC 기반 내부 서비스 통신
- 🌐 REST API 외부 인터페이스
- 🐳 Docker & Docker Compose 설정
- 🗄️ MySQL 데이터베이스 연동
- 📨 Kafka 메시지 큐 통합
- 💳 복합 결제 시스템 (Strategy Pattern)
  - PG 결제 (Mock)
  - 캐시포인트 결제
  - 쿠폰 할인
  - BNPL 결제 (Mock)
- 📊 Swagger API 문서화
- 🔍 헬스체크 엔드포인트
- 📋 상세한 프로젝트 문서화
  - API 명세서
  - 데이터베이스 ERD
  - 구현 가이드
  - 배포 가이드
- 🛠️ 개발 및 배포 스크립트
- 🧪 API 테스트 스크립트

### 구현된 기능

#### Member Service
- 회원 정보 조회
- 캐시포인트 잔액 조회
- 캐시포인트 차감/환불
- gRPC 서버 구현

#### Product Service
- 상품 정보 조회
- 상품 목록 조회
- 재고 확인/차감/복원
- 동시성 제어를 통한 재고 관리

#### Order Service
- 주문 생성/조회/상태 변경
- 복합 결제 지원
- 서비스 간 gRPC 통신
- 비즈니스 로직 검증
- 이벤트 기반 아키텍처

#### Payment Service
- 다중 결제수단 처리
- Strategy Pattern 기반 결제 프로세서
- 보상 트랜잭션 구현
- Mock 외부 서비스 연동

### 기술적 특징
- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.2.x
- **데이터베이스**: MySQL 8.0
- **메시지 큐**: Apache Kafka
- **통신**: gRPC (내부), REST API (외부)
- **컨테이너화**: Docker
- **빌드 도구**: Gradle

### 결제 시스템 규칙
- 단일 결제: PG, 캐시포인트, BNPL 지원 (쿠폰 단독 불가)
- 복합 결제: PG + 캐시포인트/쿠폰 조합 (BNPL 복합 불가)
- 캐시포인트 잔액 부족 시 결제 실패
- 재고 부족 시 주문 생성 실패

### 데이터베이스 스키마
- 8개 테이블 설계 (members, products, orders, order_items, payments, payment_methods, coupons, member_coupons)
- 외래키 제약조건으로 데이터 무결성 보장
- 적절한 인덱스 설계로 성능 최적화

### 문서화
- 포괄적인 README.md
- API 명세서 (Swagger 포함)
- 데이터베이스 ERD (Mermaid 다이어그램)
- 상세한 구현 가이드
- 단계별 배포 가이드
- 개발 고려사항 문서

### 개발 도구
- Docker Compose 기반 로컬 개발 환경
- 자동화된 빌드 스크립트
- API 테스트 스크립트
- Gradle 래퍼 포함

---

## 향후 계획

### v1.1.0 (예정)
- [ ] JWT 기반 인증/인가 시스템
- [ ] Redis 캐시 레이어 추가
- [ ] 성능 모니터링 (Prometheus + Grafana)
- [ ] 통합 테스트 자동화

### v1.2.0 (예정)
- [ ] 실제 PG 및 BNPL 서비스 연동
- [ ] 고급 쿠폰 시스템 (조건부 쿠폰, 중복 사용 등)
- [ ] 주문 배송 상태 관리
- [ ] 관리자 대시보드

### v2.0.0 (예정)
- [ ] 마이크로서비스 오케스트레이션 (Kubernetes)
- [ ] 서비스 메시 도입 (Istio)
- [ ] 분산 추적 시스템
- [ ] 멀티 테넌트 지원

---

> **참고**: 이 프로젝트는 학습 및 포트폴리오 목적으로 개발되었으며, 
> 프로덕션 환경에서 사용하기 위해서는 추가적인 보안 강화 및 성능 최적화가 필요합니다.
