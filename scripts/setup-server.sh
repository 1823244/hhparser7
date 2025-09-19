#!/bin/bash

# Скрипт первоначальной настройки Ubuntu 20.04.5 сервера для деплоя hhparser5
# Запускать от имени root или с sudo

set -e

echo "🚀 Начинаем настройку сервера для hhparser5..."

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция логирования
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

# Проверка root прав
if [[ $EUID -ne 0 ]]; then
   error "Этот скрипт должен запускаться с правами root (sudo)"
fi

# Обновление системы
log "Обновляем систему..."
apt update && apt upgrade -y

# Установка необходимых пакетов
log "Устанавливаем необходимые пакеты..."
apt install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    htop \
    vim \
    ufw \
    fail2ban \
    unzip \
    wget

# Установка Docker
log "Устанавливаем Docker..."
if ! command -v docker &> /dev/null; then
    # Удаляем старые версии
    apt remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true
    
    # Добавляем официальный GPG ключ Docker
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # Добавляем репозиторий Docker
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Устанавливаем Docker
    apt update
    apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    
    # Запускаем и включаем Docker
    systemctl start docker
    systemctl enable docker
    
    log "Docker установлен успешно"
else
    log "Docker уже установлен"
fi

# Установка Docker Compose (standalone)
log "Устанавливаем Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_VERSION="v2.21.0"
    curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    log "Docker Compose установлен"
else
    log "Docker Compose уже установлен"
fi

# Создание пользователя для приложения
log "Создаем пользователя hhparser..."
if ! id "hhparser" &>/dev/null; then
    useradd -m -s /bin/bash hhparser
    usermod -aG docker hhparser
    log "Пользователь hhparser создан"
else
    log "Пользователь hhparser уже существует"
fi

# Создание директорий
log "Создаем директории для приложения..."
mkdir -p /opt/hhparser
mkdir -p /var/log/hhparser
mkdir -p /opt/hhparser/backups

# Настройка прав доступа
chown -R hhparser:hhparser /opt/hhparser
chown -R hhparser:hhparser /var/log/hhparser

# Настройка UFW (файрвол)
log "Настраиваем файрвол..."
ufw --force reset
ufw default deny incoming
ufw default allow outgoing

# Разрешаем SSH
ufw allow ssh

# Разрешаем HTTP и HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Разрешаем порт приложения (только для тестирования)
ufw allow 9595/tcp

# Включаем файрвол
ufw --force enable

# Настройка Fail2ban
log "Настраиваем Fail2ban..."
cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 5

[sshd]
enabled = true
port = ssh
logpath = /var/log/auth.log
EOF

systemctl restart fail2ban
systemctl enable fail2ban

# Настройка логротации
log "Настраиваем ротацию логов..."
cat > /etc/logrotate.d/hhparser << 'EOF'
/var/log/hhparser/*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    copytruncate
}
EOF

# Создание systemd сервиса для автозапуска приложения
log "Создаем systemd сервис..."
cat > /etc/systemd/system/hhparser.service << 'EOF'
[Unit]
Description=HHParser5 Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/hhparser
ExecStart=/usr/local/bin/docker-compose -f docker-compose.prod.yml up -d
ExecStop=/usr/local/bin/docker-compose -f docker-compose.prod.yml down
User=hhparser
Group=hhparser

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload

# Установка мониторинга ресурсов
log "Устанавливаем мониторинг..."
apt install -y htop iotop nethogs

# Оптимизация системы для Java приложений
log "Оптимизируем систему для Java..."
echo "vm.swappiness=10" >> /etc/sysctl.conf
echo "vm.max_map_count=262144" >> /etc/sysctl.conf
sysctl -p

# Создание скрипта для бэкапа БД
log "Создаем скрипт бэкапа БД..."
cat > /opt/hhparser/backup-db.sh << 'EOF'
#!/bin/bash

# Скрипт бэкапа PostgreSQL базы данных
BACKUP_DIR="/opt/hhparser/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="hhparser5"
BACKUP_FILE="${BACKUP_DIR}/hhparser5_backup_${DATE}.sql"

# Создаем бэкап
docker exec hhparser-postgres pg_dump -U postgres -d ${DB_NAME} > "${BACKUP_FILE}"

# Сжимаем бэкап
gzip "${BACKUP_FILE}"

# Удаляем старые бэкапы (старше 7 дней)
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +7 -delete

echo "Backup completed: ${BACKUP_FILE}.gz"
EOF

chmod +x /opt/hhparser/backup-db.sh
chown hhparser:hhparser /opt/hhparser/backup-db.sh

# Добавляем задачу в cron для автоматического бэкапа
log "Настраиваем автоматический бэкап..."
(crontab -u hhparser -l 2>/dev/null; echo "0 2 * * * /opt/hhparser/backup-db.sh") | crontab -u hhparser -

# Вывод информации о системе
log "Настройка сервера завершена!"
echo ""
echo "📋 Информация о системе:"
echo "  🐧 OS: $(lsb_release -d | cut -f2)"
echo "  🐳 Docker: $(docker --version)"
echo "  📦 Docker Compose: $(docker-compose --version)"
echo "  🔥 UFW Status: $(ufw status | head -1)"
echo ""
echo "📁 Структура директорий:"
echo "  /opt/hhparser/          - Основная директория приложения"
echo "  /var/log/hhparser/      - Логи приложения"
echo "  /opt/hhparser/backups/  - Бэкапы БД"
echo ""
echo "👤 Пользователь 'hhparser' создан и добавлен в группу docker"
echo ""
echo "🔄 Следующие шаги:"
echo "  1. Скопируйте файлы приложения в /opt/hhparser/"
echo "  2. Создайте файл .env с настройками"
echo "  3. Запустите приложение: sudo systemctl start hhparser"
echo ""
echo "🎉 Сервер готов к деплою!"
