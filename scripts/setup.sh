#!/bin/bash

# 캐시노트 마켓 주문 서비스 초기 설정 스크립트

set -e

echo "🚀 캐시노트 마켓 주문 서비스 설정을 시작합니다..."

# Java 버전 확인
echo "📋 Java 버전 확인 중..."
if ! command -v java &> /dev/null; then
    echo "❌ Java가 설치되어 있지 않습니다. JDK 17 이상을 설치해주세요."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 이상이 필요합니다. 현재 버전: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java 버전 확인 완료: $JAVA_VERSION"

# Docker 확인
echo "🐳 Docker 확인 중..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker가 설치되어 있지 않습니다. Docker를 설치해주세요."
    exit 1
fi
echo "✅ Docker 확인 완료"

# Docker Compose 확인
echo "🐳 Docker Compose 확인 중..."
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose가 설치되어 있지 않습니다. Docker Compose를 설치해주세요."
    exit 1
fi
echo "✅ Docker Compose 확인 완료"

# Gradle Wrapper 권한 설정
echo "⚙️ Gradle Wrapper 권한 설정 중..."
chmod +x gradlew
echo "✅ Gradle Wrapper 권한 설정 완료"

# Gradle Wrapper 다운로드
echo "📦 Gradle Wrapper 다운로드 중..."
./gradlew --version
echo "✅ Gradle Wrapper 다운로드 완료"

# 프로젝트 빌드
echo "🔨 프로젝트 빌드 중..."
./gradlew clean build
echo "✅ 프로젝트 빌드 완료"

# Docker 이미지 빌드
echo "🐳 Docker 이미지 빌드 중..."
docker-compose build
echo "✅ Docker 이미지 빌드 완료"

# 인프라 서비스 시작
echo "🚀 인프라 서비스 시작 중..."
docker-compose up -d mysql kafka zookeeper
echo "⏳ 인프라 서비스 초기화 대기 중... (30초)"
sleep 30

# 애플리케이션 서비스 시작
echo "🚀 애플리케이션 서비스 시작 중..."
docker-compose up -d

# 서비스 상태 확인
echo "📊 서비스 상태 확인 중..."
sleep 20
docker-compose ps

# Health Check
echo "🔍 헬스체크 수행 중..."
for port in 8081 8082 8083 8084; do
    echo "Checking service on port $port..."
    for i in {1..30}; do
        if curl -f http://localhost:$port/actuator/health &> /dev/null; then
            echo "✅ Service on port $port is healthy"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "❌ Service on port $port is not responding"
        fi
        sleep 2
    done
done

echo ""
echo "🎉 설정이 완료되었습니다!"
echo ""
echo "📖 서비스 엔드포인트:"
echo "   - Member Service: http://localhost:8081/swagger-ui.html"
echo "   - Product Service: http://localhost:8082/swagger-ui.html"
echo "   - Order Service: http://localhost:8083/swagger-ui.html"
echo "   - Payment Service: http://localhost:8084/swagger-ui.html"
echo ""
echo "🔧 유용한 명령어:"
echo "   - 전체 로그 보기: docker-compose logs -f"
echo "   - 특정 서비스 로그: docker-compose logs -f member-service"
echo "   - 서비스 재시작: docker-compose restart [service-name]"
echo "   - 전체 중지: docker-compose down"
echo "   - 데이터 포함 중지: docker-compose down -v"
echo ""
echo "🧪 API 테스트 예시:"
echo "   curl -X GET 'http://localhost:8081/api/v1/members/1'"
echo "   curl -X GET 'http://localhost:8082/api/v1/products/1'"
echo ""
