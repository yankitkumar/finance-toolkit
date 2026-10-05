FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/appointment-service.jar app.jar
EXPOSE 9102
ENTRYPOINT ["java", "-jar", "app.jar"]
