#!/bin/bash

# 프로젝트 정리 스크립트

set -e

echo "🧹 프로젝트 정리 시작"

# Docker 컨테이너 및 볼륨 정리
echo "🐳 Docker 리소스 정리..."
docker-compose down -v
docker system prune -f

# Gradle 빌드 파일 정리
echo "🏗️ Gradle 빌드 파일 정리..."
./gradlew clean

# 로그 파일 정리
echo "📄 로그 파일 정리..."
find . -name "*.log" -type f -delete

# 임시 파일 정리
echo "🗂️ 임시 파일 정리..."
find . -name ".DS_Store" -type f -delete
find . -name "Thumbs.db" -type f -delete

echo "✅ 정리 완료!"
