#!/bin/bash

# Скрипт деплоя приложения hhparser5 на продакшен сервер
# Запускать на сервере от имени пользователя hhparser

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Настройки
APP_DIR="/opt/hhparser"
BACKUP_DIR="${APP_DIR}/backups"
REGISTRY="ghcr.io"
IMAGE_NAME="1823244/hhparser7"  # Замените на ваш GitHub username
IMAGE_TAG="${1:-latest}"

# Функции логирования
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

# Проверка прав пользователя
if [[ $EUID -eq 0 ]]; then
   error "Этот скрипт НЕ должен запускаться от root. Используйте пользователя hhparser"
fi

# Проверка наличия Docker
if ! command -v docker &> /dev/null; then
    error "Docker не установлен"
fi

# Переход в рабочую директорию
cd "${APP_DIR}" || error "Не удается перейти в директорию ${APP_DIR}"

log "🚀 Начинаем деплой hhparser5..."
info "Образ: ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"

# Функция проверки здоровья приложения
check_health() {
    local max_attempts=30
    local attempt=1
    
    log "Проверяем здоровье приложения..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s http://localhost:9595/actuator/health > /dev/null; then
            log "✅ Приложение работает корректно"
            return 0
        fi
        
        info "Попытка $attempt/$max_attempts: приложение еще не готово..."
        sleep 10
        ((attempt++))
    done
    
    error "❌ Приложение не отвечает после $max_attempts попыток"
}

# Функция создания бэкапа БД
create_backup() {
    if docker ps | grep -q "hhparser-postgres"; then
        log "Создаем бэкап базы данных..."
        ./backup-db.sh || warn "Не удалось создать бэкап БД"
    else
        warn "PostgreSQL контейнер не запущен, пропускаем бэкап"
    fi
}

# Функция отката к предыдущей версии
rollback() {
    warn "Начинаем откат к предыдущей версии..."
    
    # Получаем предыдущий образ
    local previous_image=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" | grep "${REGISTRY}/${IMAGE_NAME}" | grep -v "latest" | head -1 | awk '{print $1}')
    
    if [ -n "$previous_image" ]; then
        info "Откатываемся к образу: $previous_image"
        
        # Тегируем предыдущий образ как latest
        docker tag "$previous_image" "${REGISTRY}/${IMAGE_NAME}:latest"
        
        # Перезапускаем контейнеры
        docker-compose -f docker-compose.prod.yml down
        docker-compose -f docker-compose.prod.yml up -d
        
        check_health && log "✅ Откат выполнен успешно" || error "❌ Откат не удался"
    else
        error "Не найден предыдущий образ для отката"
    fi
}

# Обработчик сигналов для отката при ошибке
trap 'error "Деплой прерван! Выполняем откат..."; rollback' ERR

# Проверка наличия .env файла
if [ ! -f ".env" ]; then
    warn ".env файл не найден, используем значения по умолчанию"
    warn "Рекомендуется создать .env файл с настройками продакшена"
fi

# Проверка наличия docker-compose.prod.yml
if [ ! -f "docker-compose.prod.yml" ]; then
    error "Файл docker-compose.prod.yml не найден"
fi

# Логинимся в registry если нужно (для приватных репозиториев)
if [ -n "${GITHUB_TOKEN}" ]; then
    log "Выполняем вход в GitHub Container Registry..."
    echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_USERNAME}" --password-stdin
fi

# Создаем бэкап перед деплоем
create_backup

# Получаем информацию о текущих контейнерах
log "Текущее состояние контейнеров:"
docker-compose -f docker-compose.prod.yml ps || true

# Останавливаем текущие контейнеры
log "Останавливаем текущие контейнеры..."
docker-compose -f docker-compose.prod.yml down

# Обновляем образы
log "Обновляем Docker образы..."
if [ "${IMAGE_TAG}" != "latest" ]; then
    # Если указан конкретный тег, скачиваем его
    docker pull "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    docker tag "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}" "${REGISTRY}/${IMAGE_NAME}:latest"
else
    # Иначе обновляем все образы
    docker-compose -f docker-compose.prod.yml pull
fi

# Очищаем неиспользуемые образы
log "Очищаем старые образы..."
docker image prune -f

# Запускаем обновленные контейнеры
log "Запускаем обновленные контейнеры..."
docker-compose -f docker-compose.prod.yml up -d

# Проверяем статус контейнеров
log "Статус контейнеров:"
docker-compose -f docker-compose.prod.yml ps

# Проверяем здоровье приложения
check_health

# Показываем логи для диагностики
log "Последние логи приложения:"
docker-compose -f docker-compose.prod.yml logs --tail=20 hhparser-app

# Очищаем кэш Docker для экономии места
log "Очищаем Docker кэш..."
docker system prune -f

# Отключаем обработчик ошибок (деплой успешен)
trap - ERR

log "🎉 Деплой завершен успешно!"
info "Приложение доступно по адресу: http://193.108.113.75:9595"
info "Мониторинг доступен по адресу: http://193.108.113.75:9090 (Prometheus)"
info "Графики доступны по адресу: http://193.108.113.75:3000 (Grafana)"

# Показываем использование ресурсов
echo ""
log "📊 Использование ресурсов:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
