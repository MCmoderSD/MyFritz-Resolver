FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY /MyFritz-Resolver.jar /app/MyFritz-Resolver.jar
ENTRYPOINT ["java", "-jar", "/app/MyFritz-Resolver.jar", "/app/config.json"]