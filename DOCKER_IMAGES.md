# 🐳 Справочник Docker образов для hhparser5

Документация по рекомендуемым Docker образам для проекта.

## 📦 Образы для сборки (Maven + JDK)

### Рекомендуемые образы Eclipse Temurin:

```dockerfile
# Alpine (минималистичный, быстрый)
FROM maven:3.9.6-eclipse-temurin-17-alpine

# Ubuntu Focal (больше совместимости)
FROM maven:3.9.6-eclipse-temurin-17-focal

# Ubuntu Jammy (новая LTS версия)
FROM maven:3.9.6-eclipse-temurin-17-jammy
```

### Альтернативные образы:

```dockerfile
# Amazon Corretto (если нужна поддержка AWS)
FROM maven:3.9.6-amazoncorretto-17-alpine
FROM maven:3.9.6-amazoncorretto-17-ubuntu

# Microsoft OpenJDK (если используете Azure)
FROM maven:3.9.6-microsoft-openjdk-17-ubuntu
```

## 🚀 Образы для выполнения (JRE только)

### Рекомендуемые образы Eclipse Temurin:

```dockerfile
# Alpine (минимальный размер ~100MB)
FROM eclipse-temurin:17-jre-alpine

# Ubuntu Jammy (больше совместимости ~200MB)
FROM eclipse-temurin:17-jre-jammy

# Ubuntu Focal (стабильная LTS)
FROM eclipse-temurin:17-jre-focal
```

### Специализированные образы:

```dockerfile
# Distroless (максимальная безопасность)
FROM gcr.io/distroless/java17-debian11

# Red Hat UBI (для Enterprise)
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime
```

## 📊 Сравнение размеров образов

| Образ | Приблизительный размер | Рекомендация |
|-------|----------------------|--------------|
| `eclipse-temurin:17-jre-alpine` | ~100MB | ✅ Основной выбор |
| `eclipse-temurin:17-jre-jammy` | ~200MB | ✅ Для совместимости |
| `amazoncorretto:17-alpine` | ~120MB | 🟡 Для AWS |
| `distroless/java17` | ~180MB | 🟡 Для безопасности |

## 🔄 Миграция с устаревших образов

### Устаревшие образы (не используйте):

```dockerfile
# ❌ Больше не доступны
FROM openjdk:17-jre-slim
FROM maven:3.9.6-openjdk-17-slim
FROM adoptopenjdk:17-jre-hotspot-bionic
```

### Замена на актуальные:

```dockerfile
# ✅ Вместо openjdk:17-jre-slim
FROM eclipse-temurin:17-jre-alpine

# ✅ Вместо maven:3.9.6-openjdk-17-slim  
FROM maven:3.9.6-eclipse-temurin-17-alpine

# ✅ Вместо adoptopenjdk
FROM eclipse-temurin:17-jre-alpine
```

## 🛠️ Настройки для разных сред

### Для продакшена (текущая конфигурация):

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
# ... build stage ...
FROM eclipse-temurin:17-jre-alpine
```

### Для разработки (больше инструментов):

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-jammy AS builder
# ... build stage ...
FROM eclipse-temurin:17-jre-jammy
```

### Для максимальной безопасности:

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
# ... build stage ...
FROM gcr.io/distroless/java17-debian11
```

## 📝 Примечания

- **Eclipse Temurin** - рекомендуемый дистрибутив OpenJDK
- **Alpine** образы меньше по размеру, но могут иметь проблемы совместимости
- **Ubuntu/Debian** образы больше, но более совместимы
- **Distroless** образы максимально безопасны, но сложнее в отладке

## 🔍 Проверка доступности образов

```bash
# Проверить доступность образа
docker pull maven:3.9.6-eclipse-temurin-17-alpine
docker pull eclipse-temurin:17-jre-alpine

# Посмотреть размер образа
docker images | grep temurin

# Проверить метаданные
docker inspect eclipse-temurin:17-jre-alpine
```

---
*Последнее обновление: $(date +%Y-%m-%d)*
