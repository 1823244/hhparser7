# 🔑 Настройка GitHub Secrets для CI/CD

Пошаговая инструкция по настройке GitHub Secrets для автоматического деплоя hhparser7.

## 📋 Необходимые Secrets

Для работы CI/CD пайплайна необходимо добавить следующие secrets в ваш GitHub репозиторий:

### 🔐 Обязательные Secrets:

| Secret Name | Описание | Пример значения |
|-------------|----------|-----------------|
| `PROD_HOST` | IP адрес или домен продакшен сервера | `193.108.113.75` |
| `PROD_USER` | Пользователь для SSH подключения | `hhparser` |
| `PROD_PORT` | Порт SSH (обычно 22) | `22` |
| `PROD_SSH_KEY` | Приватный SSH ключ для подключения | `-----BEGIN OPENSSH PRIVATE KEY-----\n...` |

### 🔧 Дополнительные Secrets (для переменных окружения):

| Secret Name | Описание | Обязательность |
|-------------|----------|----------------|
| `POSTGRES_PASSWORD` | Пароль PostgreSQL | Рекомендуется |
| `RABBITMQ_HOST` | Хост RabbitMQ | Опционально |
| `RABBITMQ_USERNAME` | Пользователь RabbitMQ | Опционально |
| `RABBITMQ_PASSWORD` | Пароль RabbitMQ | Опционально |
| `RABBITMQ_VHOST` | Virtual host RabbitMQ | Опционально |
| `GRAFANA_PASSWORD` | Пароль для Grafana | Опционально |

## 🛠️ Как добавить Secrets

### Шаг 1: Переход к настройкам

1. Откройте ваш репозиторий на GitHub
2. Перейдите в **Settings** (в верхнем меню репозитория)
3. В левом меню выберите **Secrets and variables** → **Actions**

### Шаг 2: Добавление Secrets

1. Нажмите **New repository secret**
2. Введите **Name** (например, `PROD_HOST`)
3. Введите **Secret** (например, `193.108.113.75`)
4. Нажмите **Add secret**
5. Повторите для всех необходимых secrets

## 🔑 Генерация и настройка SSH ключа

### На вашей локальной машине:

```bash
# 1. Генерация SSH ключа (если еще нет)
ssh-keygen -t ed25519 -C "github-actions@hhparser7" -f ~/.ssh/hhparser7_key

# 2. Скопируйте приватный ключ (это будет PROD_SSH_KEY)
cat ~/.ssh/hhparser7_key

# 3. Скопируйте публичный ключ для сервера
cat ~/.ssh/hhparser7_key.pub
```

### На продакшен сервере:

```bash
# 1. Подключитесь к серверу как root
ssh root@193.108.113.75

# 2. Переключитесь на пользователя hhparser
su - hhparser

# 3. Создайте директорию для SSH ключей
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# 4. Добавьте публичный ключ в authorized_keys
echo "ваш_публичный_ключ_здесь" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# 5. Проверьте подключение с локальной машины
ssh -i ~/.ssh/hhparser7_key hhparser@193.108.113.75
```

## 📝 Пример значений Secrets

### Основные Secrets:

```
PROD_HOST: 193.108.113.75
PROD_USER: hhparser
PROD_PORT: 22
PROD_SSH_KEY: -----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAFwAAAAdzc2gtcn
NhAAAAAwEAAQAAAQEAy8Jsf9AIFHEfNOJ8A/LoTIEuP8Qc...
...весь приватный ключ...
-----END OPENSSH PRIVATE KEY-----
```

### Дополнительные Secrets:

```
POSTGRES_PASSWORD: your_strong_password_123
RABBITMQ_HOST: cow-01.rmq2.cloudamqp.com
RABBITMQ_USERNAME: your_rabbitmq_user
RABBITMQ_PASSWORD: your_rabbitmq_password
RABBITMQ_VHOST: your_vhost
GRAFANA_PASSWORD: grafana_admin_password
```

## ⚠️ Важные моменты безопасности

### 🔐 SSH ключ:

- ✅ Используйте отдельный SSH ключ только для GitHub Actions
- ✅ Не используйте свой личный SSH ключ
- ✅ Приватный ключ должен включать заголовки `-----BEGIN` и `-----END`
- ✅ Скопируйте ключ полностью, включая переносы строк

### 🛡️ Пароли:

- ✅ Используйте сильные пароли (минимум 12 символов)
- ✅ Не используйте одинаковые пароли для разных сервисов
- ✅ Регулярно обновляйте пароли

### 🌐 Сеть:

- ✅ Убедитесь, что порт SSH (22) открыт в файрволе
- ✅ Проверьте, что сервер доступен извне
- ✅ Рассмотрите использование нестандартного SSH порта

## 🧪 Тестирование настроек

### Проверка SSH подключения:

```bash
# Тест с локальной машины
ssh -i ~/.ssh/hhparser7_key hhparser@193.108.113.75 "echo 'SSH работает!'"
```

### Ручной запуск GitHub Actions:

1. Перейдите в **Actions** в вашем репозитории
2. Выберите workflow **Manual Deploy to Production**
3. Нажмите **Run workflow**
4. Проверьте логи выполнения

## 🔍 Диагностика проблем

### Частые ошибки:

#### "missing server host"
- ✅ Проверьте, что `PROD_HOST` добавлен в Secrets
- ✅ Убедитесь, что имя secret написано правильно

#### "Permission denied (publickey)"
- ✅ Проверьте правильность приватного ключа в `PROD_SSH_KEY`
- ✅ Убедитесь, что публичный ключ добавлен в `~/.ssh/authorized_keys`
- ✅ Проверьте права доступа: `chmod 600 ~/.ssh/authorized_keys`

#### "Connection refused"
- ✅ Проверьте, что сервер запущен и доступен
- ✅ Убедитесь, что порт SSH открыт в файрволе
- ✅ Проверьте правильность IP адреса

### Логи для диагностики:

```bash
# На сервере - проверка SSH логов
sudo tail -f /var/log/auth.log

# Локально - подробный SSH лог
ssh -vvv -i ~/.ssh/hhparser7_key hhparser@193.108.113.75
```

## ✅ Проверочный список

Перед запуском деплоя убедитесь:

- [ ] ✅ Все 4 основных secrets добавлены в GitHub
- [ ] ✅ SSH ключ сгенерирован и настроен
- [ ] ✅ Публичный ключ добавлен на сервер
- [ ] ✅ SSH подключение работает с локальной машины
- [ ] ✅ Сервер настроен через `setup-server.sh`
- [ ] ✅ Файлы деплоя скопированы через `copy-to-server.sh`
- [ ] ✅ Environment variables настроены в `.env`

После выполнения всех пунктов GitHub Actions должен работать корректно! 🚀

---

> 💡 **Совет**: Начните с ручного деплоя через `Manual Deploy to Production` workflow для проверки всех настроек.
