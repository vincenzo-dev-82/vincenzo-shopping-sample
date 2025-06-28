#!/bin/bash

# 모든 서비스 중지 스크립트

echo "🛑 Stopping all services..."

# 루트 디렉토리로 이동
cd "$(dirname "$0")/.."

# 모든 컨테이너 중지
docker-compose down

echo "✅ All services stopped."
echo "🗑️ To remove volumes (data will be lost): docker-compose down -v"
