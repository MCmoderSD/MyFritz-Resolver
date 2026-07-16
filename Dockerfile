
# Base Image JRE 25
FROM eclipse-temurin:25-jre-alpine

# Set Up
WORKDIR /app
COPY /MyFritz-Resolver.jar /app/MyFritz-Resolver.jar

# Run Application
ENTRYPOINT ["java", "-jar", "/app/MyFritz-Resolver.jar", "/app/config.json"]