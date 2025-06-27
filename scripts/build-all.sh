#!/bin/bash

# 전체 프로젝트 빌드 스크립트

set -e

echo "🔨 캐시노트 마켓 프로젝트 빌드 시작"
echo "======================================"

# 루트 프로젝트에서 전체 빌드
echo "📦 Gradle 빌드 실행..."
./gradlew clean build -x test

echo "\n🧪 테스트 실행..."
./gradlew test

echo "\n🐳 Docker 이미지 빌드..."
docker-compose build

echo "\n✅ 빌드 완료!"
echo "======================================"
echo "다음 명령으로 서비스를 시작하세요:"
echo "docker-compose up -d"
