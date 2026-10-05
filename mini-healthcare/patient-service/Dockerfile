FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/patient-service.jar app.jar
EXPOSE 9101
ENTRYPOINT ["java", "-jar", "app.jar"]
