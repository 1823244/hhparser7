# Dockerfile для hhparser5 приложения
# Используем многоступенчатую сборку для оптимизации размера образа

# Этап 1: Сборка приложения
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Maven для кэширования зависимостей
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Загружаем зависимости (это будет кэшироваться Docker)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение с профилем для продакшена
RUN mvn clean package -Dmaven.test.skip=true -Pruvds

# Этап 2: Создание финального образа
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем curl для healthcheck и создаем пользователя для безопасности
RUN apk add --no-cache curl && \
    addgroup -S hhparser && \
    adduser -S hhparser -G hhparser

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем директории для логов и данных
RUN mkdir -p /log-hhparser5 && \
    chown -R hhparser:hhparser /log-hhparser5 && \
    chown -R hhparser:hhparser /app

# Копируем собранное приложение из первого этапа
COPY --from=builder /app/target-ruvds/hhparser5.jar /app/hhparser5.jar

# Изменяем владельца файлов
RUN chown hhparser:hhparser /app/hhparser5.jar

# Переключаемся на непривилегированного пользователя
USER hhparser

# Открываем порт приложения
EXPOSE 9595

# Настройки JVM для контейнера
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseContainerSupport"

# Переменные для профиля
ENV SPRING_PROFILES_ACTIVE=ruvds

# Проверка здоровья приложения
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9595/actuator/health || exit 1

# Запуск приложения
CMD ["sh", "-c", "java $JAVA_OPTS -jar hhparser5.jar"]
