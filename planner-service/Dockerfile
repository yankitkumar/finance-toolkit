# Small Java 21 runtime image. Build the jar first with: mvn package
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/planner-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
