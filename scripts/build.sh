#!/bin/bash

# 캐시노트 마켓 주문 서비스 빌드 스크립트

set -e

echo "🚀 빌드를 시작합니다..."

# 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

# Gradle 권한 설정
chmod +x gradlew

# 모든 서비스 빌드
echo "📦 모든 서비스를 빌드합니다..."
./gradlew clean build -x test

# Docker 이미지 빌드
echo "🐳 Docker 이미지를 빌드합니다..."
docker-compose build

echo "✅ 빌드가 완료되었습니다!"
echo "💡 서비스를 시작하려면 다음 명령어를 실행하세요:"
echo "   docker-compose up -d"
