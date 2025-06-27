#!/bin/bash

# 전체 환경 정리 스크립트

set -e

echo "🧹 캐시노트 마켓 환경 정리 시작"
echo "======================================"

# Docker 컨테이너 및 볼륨 정리
echo "🐳 Docker 컨테이너 중지 및 삭제..."
docker-compose down -v --remove-orphans

# Docker 이미지 정리
echo "\n🗑️ Docker 이미지 삭제..."
docker-compose down --rmi all

# 빌드 아티팩트 정리
echo "\n📦 Gradle 빌드 캐시 정리..."
./gradlew clean

# Docker 볼륨 정리 (데이터베이스 데이터 포함)
echo "\n💾 Docker 볼륨 정리..."
docker volume prune -f

echo "\n✅ 환경 정리 완료!"
echo "======================================"
echo "새로 시작하려면 다음 명령을 실행하세요:"
echo "./scripts/build-all.sh"
echo "docker-compose up -d"
