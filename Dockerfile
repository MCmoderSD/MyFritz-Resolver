# Base Image JRE 25
FROM eclipse-temurin:25-jre-alpine

# Unprivileged runtime user
RUN addgroup -S app && adduser -S -G app app

# Set Up
WORKDIR /app
COPY app.jar /app/app.jar
USER app

# Run Application
ENTRYPOINT ["java", "-jar", "/app/app.jar", "/app/config.json"]