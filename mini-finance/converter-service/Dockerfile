FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/converter-service.jar app.jar
EXPOSE 9002
ENTRYPOINT ["java", "-jar", "app.jar"]
