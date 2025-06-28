#!/bin/bash

# 환경 정리 스크립트

set -e

echo "🧹 캐시노트 마켓 환경 정리를 시작합니다..."

# 선택적 정리 메뉴
echo "정리 옵션을 선택하세요:"
echo "1) 서비스만 중지 (데이터 보존)"
echo "2) 서비스 중지 + Docker 이미지 삭제"
echo "3) 전체 정리 (데이터베이스 포함)"
echo "4) 개발 데이터만 초기화"
read -p "선택 (1-4): " choice

case $choice in
    1)
        echo "📦 서비스 중지 중..."
        docker-compose down
        echo "✅ 서비스가 중지되었습니다. 데이터는 보존됩니다."
        ;;
    2)
        echo "📦 서비스 중지 및 이미지 삭제 중..."
        docker-compose down
        docker-compose down --rmi all
        echo "✅ 서비스 중지 및 이미지가 삭제되었습니다."
        ;;
    3)
        echo "🗑️ 전체 환경 정리 중..."
        docker-compose down -v
        docker-compose down --rmi all
        docker system prune -f
        echo "✅ 모든 데이터와 이미지가 삭제되었습니다."
        ;;
    4)
        echo "🔄 개발 데이터 초기화 중..."
        docker-compose down
        docker volume rm vincenzo-shopping-sample_mysql_data 2>/dev/null || true
        docker volume rm vincenzo-shopping-sample_mysql_dev_data 2>/dev/null || true
        echo "✅ 개발 데이터가 초기화되었습니다."
        echo "💡 다음 실행 시 초기 데이터로 시작됩니다."
        ;;
    *)
        echo "❌ 잘못된 선택입니다."
        exit 1
        ;;
esac

# 사용하지 않는 Docker 리소스 정리
echo "\n🧹 사용하지 않는 Docker 리소스 정리 중..."
docker container prune -f
docker network prune -f
docker image prune -f

echo "\n🎉 정리가 완료되었습니다!"
echo "\n💡 다시 시작하려면:"
echo "   ./scripts/setup.sh 실행"
echo "   또는"
echo "   docker-compose up -d 실행"
