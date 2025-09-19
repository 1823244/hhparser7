# 🚀 Быстрый старт - Деплой HHParser5

Краткая инструкция для быстрого развертывания приложения на сервере Ubuntu 20.04.5.

## ⚡ За 5 минут до продакшена

### 1. Подготовьте SSH ключ

```bash
# Если нет SSH ключа
ssh-keygen -t ed25519

# Скопируйте на сервер
ssh-copy-id root@193.108.113.75
```

### 2. Настройте сервер одной командой

```bash
# Скопируйте и запустите скрипт настройки
scp scripts/setup-server.sh root@193.108.113.75:/tmp/
ssh root@193.108.113.75 "cd /tmp && chmod +x setup-server.sh && ./setup-server.sh"
```

### 3. Скопируйте файлы проекта

```bash
# Из папки проекта
chmod +x scripts/copy-to-server.sh
./scripts/copy-to-server.sh
```

### 4. Настройте переменные окружения

```bash
ssh hhparser@193.108.113.75
cd /opt/hhparser
nano .env  # Отредактируйте под ваши настройки
```

### 5. Запустите приложение

```bash
./deploy.sh
```

### 6. Проверьте работу

Откройте в браузере: http://193.108.113.75 или http://193.108.113.75:9696

## 🔧 GitHub CI/CD (опционально)

### Добавьте Secrets в GitHub:

> 📖 **Подробно**: [GITHUB_SETUP.md](GITHUB_SETUP.md)

```
Settings > Secrets and variables > Actions:

PROD_HOST=193.108.113.75
PROD_USER=hhparser  
PROD_PORT=22
PROD_SSH_KEY=<ваш приватный SSH ключ>
```

После этого каждый push в main будет автоматически деплоиться!

## 📊 Полезные команды

```bash
# Статус всех сервисов
./server-maintenance.sh status

# Мониторинг ресурсов  
./server-maintenance.sh monitor

# Просмотр логов
./server-maintenance.sh logs

# Бэкап БД
./server-maintenance.sh backup

# Перезапуск
./server-maintenance.sh restart
```

## 🌐 Доступные адреса

- **Приложение**: http://193.108.113.75 или http://193.108.113.75:9696
- **Мониторинг**: http://193.108.113.75:9090 (Prometheus)
- **Графики**: http://193.108.113.75:3000 (Grafana, admin/admin)

## 🆘 Помощь

Полная документация: [DEPLOY.md](DEPLOY.md)

---
**Всё готово! 🎉**
