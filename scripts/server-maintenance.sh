#!/bin/bash

# Скрипт для обслуживания сервера hhparser5
# Включает мониторинг, обновления, очистку и диагностику

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Настройки
APP_DIR="/opt/hhparser"
LOG_DIR="/var/log/hhparser"
BACKUP_DIR="${APP_DIR}/backups"

# Функции логирования
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

header() {
    echo -e "${PURPLE}==== $1 ====${NC}"
}

# Функция показа помощи
show_help() {
    cat << EOF
Скрипт обслуживания сервера hhparser5

Использование: $0 [КОМАНДА]

Команды:
  status      - Показать статус всех сервисов
  logs        - Показать логи приложения
  monitor     - Мониторинг ресурсов в реальном времени
  backup      - Создать бэкап базы данных
  cleanup     - Очистить систему от временных файлов
  update      - Обновить системы и Docker образы
  restart     - Перезапустить все сервисы
  health      - Проверка здоровья системы
  disk        - Анализ использования диска
  network     - Диагностика сети
  help        - Показать эту справку

Примеры:
  $0 status
  $0 monitor
  $0 backup
  $0 cleanup

EOF
}

# Функция показа статуса
show_status() {
    header "СТАТУС СЕРВИСОВ"
    
    echo "🐳 Docker:"
    docker --version
    
    echo ""
    echo "📦 Контейнеры:"
    if [ -f "${APP_DIR}/docker-compose.prod.yml" ]; then
        cd "${APP_DIR}"
        docker-compose -f docker-compose.prod.yml ps
    else
        warn "docker-compose.prod.yml не найден"
    fi
    
    echo ""
    echo "🔥 Systemd сервисы:"
    systemctl is-active docker || true
    systemctl is-active hhparser || true
    systemctl is-active fail2ban || true
    
    echo ""
    echo "🌐 Сетевые подключения:"
    ss -tulpn | grep -E ":(80|443|9595|5432|9090|3000)" || true
}

# Функция показа логов
show_logs() {
    header "ЛОГИ ПРИЛОЖЕНИЯ"
    
    if [ -f "${APP_DIR}/docker-compose.prod.yml" ]; then
        cd "${APP_DIR}"
        echo "📝 Последние логи контейнеров:"
        docker-compose -f docker-compose.prod.yml logs --tail=50 --timestamps
    else
        warn "docker-compose.prod.yml не найден"
    fi
    
    echo ""
    echo "📋 Системные логи:"
    journalctl -u docker --lines=20 --no-pager
}

# Функция мониторинга
monitor_system() {
    header "МОНИТОРИНГ РЕСУРСОВ"
    
    echo "💾 Использование памяти:"
    free -h
    
    echo ""
    echo "💽 Использование диска:"
    df -h
    
    echo ""
    echo "⚡ Загрузка процессора:"
    uptime
    
    echo ""
    echo "🐳 Статистика Docker контейнеров:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}"
    
    echo ""
    echo "🔄 Процессы:"
    ps aux --sort=-%cpu | head -10
}

# Функция создания бэкапа
create_backup() {
    header "СОЗДАНИЕ БЭКАПА"
    
    if [ -f "${APP_DIR}/backup-db.sh" ]; then
        cd "${APP_DIR}"
        ./backup-db.sh
        
        echo ""
        echo "📁 Доступные бэкапы:"
        ls -lah "${BACKUP_DIR}/"*.sql.gz 2>/dev/null | tail -10 || warn "Бэкапы не найдены"
    else
        error "Скрипт бэкапа не найден: ${APP_DIR}/backup-db.sh"
    fi
}

# Функция очистки системы
cleanup_system() {
    header "ОЧИСТКА СИСТЕМЫ"
    
    log "Очищаем Docker..."
    docker system prune -f
    docker volume prune -f
    docker image prune -a -f
    
    log "Очищаем логи..."
    journalctl --vacuum-time=7d
    
    log "Очищаем старые бэкапы (старше 30 дней)..."
    find "${BACKUP_DIR}" -name "*.sql.gz" -mtime +30 -delete 2>/dev/null || true
    
    log "Очищаем временные файлы..."
    rm -rf /tmp/*hhparser* 2>/dev/null || true
    
    log "Очищаем кэш APT..."
    if [[ $EUID -eq 0 ]]; then
        apt autoremove -y
        apt autoclean
    else
        warn "Для очистки APT кэша нужны права root"
    fi
    
    log "✅ Очистка завершена"
}

# Функция обновления системы
update_system() {
    header "ОБНОВЛЕНИЕ СИСТЕМЫ"
    
    if [[ $EUID -ne 0 ]]; then
        error "Для обновления системы нужны права root"
    fi
    
    log "Обновляем пакеты системы..."
    apt update && apt upgrade -y
    
    log "Обновляем Docker образы..."
    if [ -f "${APP_DIR}/docker-compose.prod.yml" ]; then
        cd "${APP_DIR}"
        docker-compose -f docker-compose.prod.yml pull
    fi
    
    log "✅ Обновление завершено"
    warn "Рекомендуется перезапустить приложение: $0 restart"
}

# Функция перезапуска сервисов
restart_services() {
    header "ПЕРЕЗАПУСК СЕРВИСОВ"
    
    if [ -f "${APP_DIR}/docker-compose.prod.yml" ]; then
        cd "${APP_DIR}"
        
        log "Останавливаем контейнеры..."
        docker-compose -f docker-compose.prod.yml down
        
        log "Запускаем контейнеры..."
        docker-compose -f docker-compose.prod.yml up -d
        
        sleep 10
        
        log "Проверяем статус..."
        docker-compose -f docker-compose.prod.yml ps
    else
        error "docker-compose.prod.yml не найден"
    fi
}

# Функция проверки здоровья
health_check() {
    header "ПРОВЕРКА ЗДОРОВЬЯ СИСТЕМЫ"
    
    echo "🔍 Проверяем основные сервисы..."
    
    # Проверка Docker
    if systemctl is-active --quiet docker; then
        echo "✅ Docker: Работает"
    else
        echo "❌ Docker: Не работает"
    fi
    
    # Проверка приложения
    if curl -f -s http://localhost:9595/actuator/health > /dev/null; then
        echo "✅ HHParser App: Работает"
    else
        echo "❌ HHParser App: Не отвечает"
    fi
    
    # Проверка PostgreSQL
    if docker exec hhparser-postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo "✅ PostgreSQL: Работает"
    else
        echo "❌ PostgreSQL: Не отвечает"
    fi
    
    # Проверка дискового пространства
    echo ""
    echo "💽 Проверяем дисковое пространство..."
    df -h | awk '$5 ~ /^[8-9][0-9]%|^100%/ {print "⚠️  " $0}'
    
    # Проверка памяти
    echo ""
    echo "💾 Проверяем использование памяти..."
    free -h | awk 'NR==2{printf "Память: %s/%s (%.2f%%)\n", $3,$2,$3*100/$2 }'
    
    # Проверка логов ошибок
    echo ""
    echo "📝 Проверяем логи на ошибки (последние 24 часа)..."
    journalctl --since "24 hours ago" --priority=err --no-pager | wc -l | awk '{print "Найдено ошибок: " $1}'
}

# Функция анализа диска
analyze_disk() {
    header "АНАЛИЗ ИСПОЛЬЗОВАНИЯ ДИСКА"
    
    echo "💽 Общее использование:"
    df -h
    
    echo ""
    echo "📁 Топ директорий по размеру:"
    du -h --max-depth=1 / 2>/dev/null | sort -hr | head -10
    
    echo ""
    echo "🐳 Использование Docker:"
    docker system df
    
    echo ""
    echo "📋 Большие файлы логов:"
    find /var/log -type f -size +100M -exec ls -lh {} \; 2>/dev/null | head -10
}

# Функция диагностики сети
network_diagnostics() {
    header "ДИАГНОСТИКА СЕТИ"
    
    echo "🌐 Сетевые интерфейсы:"
    ip addr show
    
    echo ""
    echo "🔗 Таблица маршрутизации:"
    ip route show
    
    echo ""
    echo "📡 Активные соединения:"
    ss -tulpn | grep LISTEN
    
    echo ""
    echo "🏥 Проверка доступности внешних сервисов:"
    ping -c 3 8.8.8.8 > /dev/null && echo "✅ DNS Google: OK" || echo "❌ DNS Google: FAIL"
    ping -c 3 google.com > /dev/null && echo "✅ Google.com: OK" || echo "❌ Google.com: FAIL"
}

# Основная логика
case "${1:-help}" in
    "status")
        show_status
        ;;
    "logs")
        show_logs
        ;;
    "monitor")
        monitor_system
        ;;
    "backup")
        create_backup
        ;;
    "cleanup")
        cleanup_system
        ;;
    "update")
        update_system
        ;;
    "restart")
        restart_services
        ;;
    "health")
        health_check
        ;;
    "disk")
        analyze_disk
        ;;
    "network")
        network_diagnostics
        ;;
    "help"|*)
        show_help
        ;;
esac
