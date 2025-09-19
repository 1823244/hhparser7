# 🚀 Процедура деплоя HHParser5 в продакшен

Документация по развертыванию Spring Boot приложения HHParser5 на Ubuntu 20.04.5 сервере с использованием Docker и GitHub Actions CI/CD.

## 📋 Содержание

- [Архитектура решения](#архитектура-решения)
- [Подготовка сервера](#подготовка-сервера)
- [Настройка GitHub Repository](#настройка-github-repository)
- [Первоначальный деплой](#первоначальный-деплой)
- [Автоматический деплой через CI/CD](#автоматический-деплой-через-cicd)
- [Мониторинг и обслуживание](#мониторинг-и-обслуживание)
- [Решение проблем](#решение-проблем)

## 🏗️ Архитектура решения

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Developer     │───▶│  GitHub Actions  │───▶│  Production     │
│   Push to main  │    │  CI/CD Pipeline  │    │  Server         │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                        ┌─────────────────┐            ▼
                        │  GitHub         │    ┌──────────────────┐
                        │  Container      │◀───│  Docker          │
                        │  Registry       │    │  - App Container │
                        └─────────────────┘    │  - PostgreSQL    │
                                              │  - Nginx         │
                                              │  - Monitoring    │
                                              └──────────────────┘
```

### Компоненты:

- **Spring Boot App**: Основное приложение (порт 9595)
- **PostgreSQL**: База данных (порт 5432)
- **Nginx**: Reverse proxy (порты 80/443)
- **Prometheus**: Мониторинг (порт 9090)
- **Grafana**: Визуализация метрик (порт 3000)

## 🔧 Подготовка сервера

### 1. Настройка SSH доступа

```bash
# Генерируем SSH ключ (если нет)
ssh-keygen -t ed25519 -C "your-email@example.com"

# Копируем публичный ключ на сервер
ssh-copy-id hhparser@193.108.113.75
```

### 2. Первоначальная настройка сервера

```bash
# Копируем скрипт настройки на сервер
scp scripts/setup-server.sh root@193.108.113.75:/tmp/

# Подключаемся к серверу и запускаем настройку
ssh root@193.108.113.75
cd /tmp
chmod +x setup-server.sh
./setup-server.sh
```

Скрипт выполнит:
- ✅ Обновление системы
- ✅ Установка Docker и Docker Compose
- ✅ Создание пользователя `hhparser`
- ✅ Настройка файрвола (UFW)
- ✅ Настройка Fail2ban
- ✅ Создание директорий и сервисов
- ✅ Настройка автоматических бэкапов

### 3. Копирование файлов деплоя

```bash
# Локально в папке проекта
./scripts/copy-to-server.sh
```

## ⚙️ Настройка GitHub Repository

### 1. Создание Secrets в GitHub

Перейдите в `Settings > Secrets and variables > Actions` и добавьте:

```
PROD_HOST=193.108.113.75
PROD_USER=hhparser
PROD_PORT=22
PROD_SSH_KEY=<содержимое приватного SSH ключа>
```

### 2. Настройка GitHub Container Registry

GitHub Actions автоматически публикует Docker образы в GitHub Container Registry при пуше в main ветку.

### 3. Настройка Environment Protection Rules (опционально)

В `Settings > Environments > production`:
- ✅ Required reviewers (для критических изменений)
- ✅ Deployment branches (только main/master)

## 🚀 Первоначальный деплой

### 1. Настройка переменных окружения

```bash
# Подключаемся к серверу
ssh hhparser@193.108.113.75
cd /opt/hhparser

# Редактируем .env файл
nano .env
```

Пример настроек `.env`:
```bash
# PostgreSQL
POSTGRES_PASSWORD=your_secure_password

# RabbitMQ (если используете CloudAMQP)
RABBITMQ_HOST=cow-01.rmq2.cloudamqp.com
RABBITMQ_USERNAME=your_username
RABBITMQ_PASSWORD=your_password
RABBITMQ_VHOST=your_vhost

# Мониторинг
GRAFANA_PASSWORD=admin_password
```

### 2. Ручной деплой для проверки

```bash
# На сервере в /opt/hhparser
./deploy.sh
```

### 3. Проверка работоспособности

```bash
# Проверяем статус
./server-maintenance.sh status

# Проверяем логи
./server-maintenance.sh logs

# Проверяем здоровье
./server-maintenance.sh health
```

Приложение будет доступно:
- 🌐 **Основное приложение**: http://193.108.113.75
- 📊 **Prometheus**: http://193.108.113.75:9090
- 📈 **Grafana**: http://193.108.113.75:3000

## 🔄 Автоматический деплой через CI/CD

### Workflow запускается при:

1. **Push в main/master ветку** - полный цикл сборки и деплоя
2. **Pull Request** - только тесты и сборка
3. **Manual trigger** - ручной запуск через GitHub Actions

### Этапы CI/CD пайплайна:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Tests     │───▶│    Build    │───▶│   Docker    │───▶│   Deploy    │
│   Maven     │    │   Package   │    │   Image     │    │   to Prod   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

1. **Test**: Запуск Maven тестов
2. **Build**: Сборка JAR файла с профилем `ruvds`
3. **Docker**: Сборка и публикация Docker образа
4. **Deploy**: Автоматический деплой на продакшен сервер

### Ручной деплой через GitHub Actions

1. Перейдите в `Actions > Manual Deploy to Production`
2. Нажмите `Run workflow`
3. Выберите параметры:
   - Environment: `production`
   - Skip tests: `false` (рекомендуется)
   - Docker tag: оставьте пустым для `latest`

## 📊 Мониторинг и обслуживание

### Доступные команды обслуживания:

```bash
# Основные команды
./server-maintenance.sh status    # Статус всех сервисов
./server-maintenance.sh monitor   # Мониторинг ресурсов
./server-maintenance.sh logs      # Просмотр логов
./server-maintenance.sh health    # Проверка здоровья системы

# Управление
./server-maintenance.sh restart   # Перезапуск сервисов
./server-maintenance.sh backup    # Создание бэкапа БД
./server-maintenance.sh cleanup   # Очистка системы

# Диагностика
./server-maintenance.sh disk      # Анализ диска
./server-maintenance.sh network   # Диагностика сети
```

### Автоматические бэкапы

Бэкапы БД создаются автоматически каждый день в 2:00 AM:
- 📁 Путь: `/opt/hhparser/backups/`
- 🗜️ Формат: `hhparser5_backup_YYYYMMDD_HHMMSS.sql.gz`
- 🔄 Хранение: 7 дней

### Мониторинг

- **Prometheus**: Сбор метрик приложения
- **Grafana**: Визуализация и алерты
- **Health checks**: Автоматические проверки работоспособности
- **Log rotation**: Автоматическая ротация логов

## 🔧 Решение проблем

### Частые проблемы и решения

#### 1. Контейнер не запускается

```bash
# Проверяем логи
docker-compose -f docker-compose.prod.yml logs hhparser-app

# Проверяем ресурсы
docker stats

# Перезапускаем
./server-maintenance.sh restart
```

#### 2. База данных недоступна

```bash
# Проверяем PostgreSQL
docker exec hhparser-postgres pg_isready -U postgres

# Проверяем подключение
docker exec hhparser-postgres psql -U postgres -c "SELECT version();"

# Восстановление из бэкапа
gunzip -c /opt/hhparser/backups/latest_backup.sql.gz | docker exec -i hhparser-postgres psql -U postgres -d hhparser5
```

#### 3. Нехватка места на диске

```bash
# Анализ использования
./server-maintenance.sh disk

# Очистка системы
./server-maintenance.sh cleanup

# Очистка Docker
docker system prune -a -f
```

#### 4. Проблемы с CI/CD

- ✅ Проверьте GitHub Secrets
- ✅ Убедитесь в доступности сервера
- ✅ Проверьте логи GitHub Actions
- ✅ Проверьте права пользователя `hhparser`

#### 5. Откат к предыдущей версии

```bash
# Автоматический откат при ошибке деплоя
./deploy.sh previous_tag

# Ручной откат
docker tag previous_image:tag current_image:latest
./server-maintenance.sh restart
```

### Контакты для поддержки

- 📧 **Email**: your-email@example.com
- 📱 **Telegram**: @your_telegram
- 🐛 **Issues**: GitHub Issues в репозитории

### Полезные ссылки

- 📚 [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- 🐳 [Docker Documentation](https://docs.docker.com/)
- 🔄 [GitHub Actions Documentation](https://docs.github.com/en/actions)
- 📊 [Prometheus Documentation](https://prometheus.io/docs/)

---

> **Примечание**: Эта документация актуальна для версии проекта от $(date). При изменениях в архитектуре обновите документацию соответственно.

## 📝 Changelog

- **v1.0.0** - Первоначальная версия процедуры деплоя
- Создана полная инфраструктура для CI/CD
- Настроены мониторинг и автоматические бэкапы
- Подготовлены скрипты обслуживания

---
**🎉 Готово к продуктивному использованию!**
