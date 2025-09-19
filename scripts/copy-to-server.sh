#!/bin/bash

# Скрипт для копирования файлов деплоя на продакшен сервер
# Запускать локально с вашей машины

set -e

# Настройки сервера (замените на ваши)
SERVER_IP="193.108.113.75"
SERVER_USER="hhparser"
SERVER_PATH="/opt/hhparser"

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

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

# Проверка наличия SSH ключа
if [ ! -f ~/.ssh/id_rsa ] && [ ! -f ~/.ssh/id_ed25519 ]; then
    error "SSH ключ не найден. Сгенерируйте ключ: ssh-keygen -t ed25519"
fi

# Проверка подключения к серверу
log "Проверяем подключение к серверу..."
if ! ssh -o ConnectTimeout=10 "${SERVER_USER}@${SERVER_IP}" "echo 'OK'" > /dev/null 2>&1; then
    error "Не удается подключиться к серверу ${SERVER_IP}"
fi

log "🚀 Копируем файлы на сервер ${SERVER_IP}..."

# Создаем директории на сервере
log "Создаем директории на сервере..."
ssh "${SERVER_USER}@${SERVER_IP}" "mkdir -p ${SERVER_PATH}/{scripts,nginx,monitoring}"

# Копируем основные файлы
log "Копируем Docker конфигурацию..."
scp docker-compose.prod.yml "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"
scp Dockerfile "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"
scp .dockerignore "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"

# Копируем конфигурацию Nginx
log "Копируем конфигурацию Nginx..."
scp -r nginx/ "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"

# Копируем конфигурацию мониторинга
log "Копируем конфигурацию мониторинга..."
scp -r monitoring/ "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"

# Копируем скрипты
log "Копируем скрипты деплоя..."
scp scripts/deploy.sh "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"
scp scripts/server-maintenance.sh "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"

# Делаем скрипты исполняемыми
log "Настраиваем права доступа..."
ssh "${SERVER_USER}@${SERVER_IP}" "chmod +x ${SERVER_PATH}/*.sh"

# Копируем пример переменных окружения
log "Копируем пример переменных окружения..."
scp env.example "${SERVER_USER}@${SERVER_IP}:${SERVER_PATH}/"

# Создаем .env файл если его нет
log "Проверяем .env файл..."
ssh "${SERVER_USER}@${SERVER_IP}" "
if [ ! -f ${SERVER_PATH}/.env ]; then
    cp ${SERVER_PATH}/env.example ${SERVER_PATH}/.env
    echo 'Создан файл .env из примера. Отредактируйте его перед запуском!'
fi
"

log "✅ Файлы скопированы успешно!"

echo ""
echo "📋 Следующие шаги:"
echo "1. Подключитесь к серверу: ssh ${SERVER_USER}@${SERVER_IP}"
echo "2. Перейдите в директорию: cd ${SERVER_PATH}"
echo "3. Отредактируйте .env файл с вашими настройками"
echo "4. Запустите деплой: ./deploy.sh"
echo ""
echo "🔧 Дополнительные команды на сервере:"
echo "  ./server-maintenance.sh status   - проверить статус"
echo "  ./server-maintenance.sh monitor  - мониторинг ресурсов"
echo "  ./server-maintenance.sh logs     - просмотр логов"
echo "  ./server-maintenance.sh backup   - создать бэкап"
echo ""
echo "🎉 Готово к деплою!"
