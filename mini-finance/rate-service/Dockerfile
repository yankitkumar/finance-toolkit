FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/rate-service.jar app.jar
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "app.jar"]
