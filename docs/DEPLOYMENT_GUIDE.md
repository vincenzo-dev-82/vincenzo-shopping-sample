# 배포 및 실행 가이드

## 시스템 요구사항

### 최소 요구사항
- **OS**: Linux, macOS, Windows (Docker 지원)
- **Memory**: 4GB RAM 이상
- **Disk**: 2GB 이상 여유 공간
- **Software**:
  - Docker 20.0 이상
  - Docker Compose 2.0 이상
  - JDK 17 이상 (로컬 개발 시)

### 권장 요구사항
- **Memory**: 8GB RAM 이상
- **CPU**: 4코어 이상
- **Disk**: 10GB 이상 여유 공간

## 빠른 시작 (Quick Start)

### 1. 저장소 클론

```bash
git clone https://github.com/vincenzo-dev-82/vincenzo-shopping-sample.git
cd vincenzo-shopping-sample
```

### 2. 전체 환경 실행

```bash
# 전체 서비스 실행 (백그라운드)
docker-compose up -d

# 실행 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

### 3. 서비스 확인

- **Member Service**: http://localhost:8081/swagger-ui.html
- **Product Service**: http://localhost:8082/swagger-ui.html
- **Order Service**: http://localhost:8083/swagger-ui.html
- **Payment Service**: http://localhost:8084/swagger-ui.html

### 4. 헬스체크

```bash
# 모든 서비스 헬스체크
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

## 단계별 실행 가이드

### 1. 인프라 서비스 먼저 실행

```bash
# MySQL, Kafka, Zookeeper만 실행
docker-compose up -d mysql kafka zookeeper

# 인프라 서비스 준비 대기 (약 30초)
sleep 30

# 연결 확인
docker exec -it vincenzo-mysql mysql -uroot -ppassword -e "SHOW DATABASES;"
```

### 2. 애플리케이션 서비스 실행

```bash
# 순서대로 실행 (의존성 고려)
docker-compose up -d member-service
sleep 10
docker-compose up -d product-service
sleep 10
docker-compose up -d payment-service
sleep 10
docker-compose up -d order-service
```

### 3. 서비스별 개별 실행

```bash
# 특정 서비스만 실행
docker-compose up -d member-service

# 특정 서비스 재시작
docker-compose restart order-service

# 특정 서비스 로그 확인
docker-compose logs -f member-service
```

## 로컬 개발 환경 설정

### 1. 사전 준비

```bash
# JDK 17 설치 확인
java -version

# 인프라 서비스만 Docker로 실행
docker-compose up -d mysql kafka zookeeper
```

### 2. 개별 서비스 실행

```bash
# Member Service
cd member-service
./gradlew bootRun

# 다른 터미널에서 Product Service
cd product-service
./gradlew bootRun

# 이하 동일하게 Order Service, Payment Service 실행
```

### 3. IDE에서 실행

**IntelliJ IDEA / VS Code**:
1. 프로젝트 import
2. JDK 17 설정
3. 각 서비스의 Application 클래스 실행

## 데이터베이스 초기화

### 자동 초기화 (Docker Compose 사용 시)

데이터베이스는 첫 실행 시 자동으로 초기화됩니다.

### 수동 초기화

```bash
# 초기 데이터 스크립트 실행
docker exec -i vincenzo-mysql mysql -uroot -ppassword vincenzo_shopping < infrastructure/init-data.sql

# 데이터 확인
docker exec -it vincenzo-mysql mysql -uroot -ppassword vincenzo_shopping -e "SELECT * FROM members;"
```

### 데이터베이스 리셋

```bash
# 데이터베이스 컨테이너 삭제 (데이터 모두 삭제)
docker-compose down -v
docker volume rm vincenzo-shopping-sample_mysql_data

# 다시 실행
docker-compose up -d mysql
```

## API 테스트

### 1. 기본 데이터 확인

```bash
# 회원 조회
curl -X GET "http://localhost:8081/api/v1/members/1"

# 상품 조회
curl -X GET "http://localhost:8082/api/v1/products/1"
```

### 2. 주문 생성 테스트

```bash
# 단일 결제 주문
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
      "paymentType": "SINGLE",
      "paymentMethods": [
        {
          "methodType": "BNPL",
          "amount": 899000.00
        }
      ]
    }
  }'
```

### 3. 복합 결제 주문 테스트

```bash
# 복합 결제 (PG + 캐시포인트)
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
          "amount": 849000.00
        },
        {
          "methodType": "CASHPOINT",
          "amount": 50000.00
        }
      ]
    }
  }'
```

## 트러블슈팅

### 1. 서비스 시작 실패

**증상**: 서비스가 시작되지 않거나 즉시 종료

**해결책**:
```bash
# 로그 확인
docker-compose logs service-name

# 포트 충돌 확인
netstat -tulpn | grep :8081

# 메모리 부족 확인
docker stats
```

### 2. 데이터베이스 연결 실패

**증상**: "Connection refused" 또는 "Unknown database"

**해결책**:
```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# MySQL 로그 확인
docker-compose logs mysql

# 데이터베이스 수동 생성
docker exec -it vincenzo-mysql mysql -uroot -ppassword -e "CREATE DATABASE IF NOT EXISTS vincenzo_shopping;"
```

### 3. gRPC 통신 실패

**증상**: 서비스 간 호출 시 "UNAVAILABLE" 에러

**해결책**:
```bash
# 네트워크 확인
docker network ls
docker network inspect vincenzo-shopping-sample_vincenzo-network

# 서비스 간 연결 테스트
docker exec vincenzo-order-service ping vincenzo-member-service

# gRPC 포트 확인
docker-compose ps
```

### 4. 메모리 부족

**증상**: 서비스 OOM(Out of Memory) 에러

**해결책**:
```bash
# Docker 메모리 설정 증가
# docker-compose.yml에서 각 서비스에 추가:
# deploy:
#   resources:
#     limits:
#       memory: 1G

# JVM 힙 메모리 조정
# Dockerfile에서:
# ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

### 5. Kafka 연결 문제

**증상**: "Broker may not be available" 에러

**해결책**:
```bash
# Kafka 및 Zookeeper 상태 확인
docker-compose ps kafka zookeeper

# Kafka 토픽 확인
docker exec vincenzo-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Kafka 재시작
docker-compose restart kafka
```

## 성능 최적화

### 1. JVM 튜닝

```dockerfile
# Dockerfile에서 JVM 옵션 추가
ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx1g", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-jar", "app.jar"]
```

### 2. 데이터베이스 최적화

```bash
# MySQL 설정 최적화 (docker-compose.yml)
command: |
  --innodb-buffer-pool-size=1G
  --innodb-log-file-size=256M
  --max-connections=200
```

### 3. 연결 풀 튜닝

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 모니터링 설정

### 1. Prometheus + Grafana 추가

```yaml
# docker-compose.yml에 추가
prometheus:
  image: prom/prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
```

### 2. 로그 중앙화 (ELK Stack)

```yaml
# docker-compose.yml에 추가
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.0.0
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false

logstash:
  image: docker.elastic.co/logstash/logstash:8.0.0
  volumes:
    - ./monitoring/logstash.conf:/usr/share/logstash/pipeline/logstash.conf

kibana:
  image: docker.elastic.co/kibana/kibana:8.0.0
  ports:
    - "5601:5601"
```

## 프로덕션 배포 고려사항

### 1. 보안 강화

```yaml
# 프로덕션용 환경변수 설정
environment:
  - DB_PASSWORD=${DB_PASSWORD}
  - JWT_SECRET=${JWT_SECRET}
  - ENCRYPTION_KEY=${ENCRYPTION_KEY}
```

### 2. 백업 전략

```bash
# 데이터베이스 백업 스크립트
#!/bin/bash
BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
docker exec vincenzo-mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD vincenzo_shopping > $BACKUP_FILE
```

### 3. 로드 밸런싱

```nginx
# nginx.conf
upstream member-service {
    server member-service-1:8081;
    server member-service-2:8081;
    server member-service-3:8081;
}

server {
    listen 80;
    location /api/v1/members {
        proxy_pass http://member-service;
    }
}
```

이 가이드를 통해 개발부터 프로덕션 배포까지 전체 라이프사이클을 관리할 수 있습니다.
